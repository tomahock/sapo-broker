package pt.com.broker.client;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.mina.core.session.IoSession;
import org.caudexorigo.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.security.SecureSessionContainer;
import pt.com.broker.security.SecureSessionInfo;
import pt.com.common.security.ClientAuthInfo;
import pt.com.types.NetAction;
import pt.com.types.NetAuthentication;
import pt.com.types.NetMessage;
import pt.com.types.NetAction.ActionType;
import pt.com.types.NetAuthentication.AuthClientAcknowledge;
import pt.com.types.NetAuthentication.AuthClientChallengeResponse;
import pt.com.types.NetAuthentication.AuthMessageType;
import pt.com.types.NetAuthentication.AuthServerChallenge;

public class ClientBrokerProtocolHandlerAuthenticationHelper
{
	private static final int MAX_WRITE_BUFFER_SIZE = 5000;
	private static final short CHALLENGE_SIZE_IN_BYTES = 8;

	private static final Logger log = LoggerFactory.getLogger(ClientBrokerProtocolHandlerAuthenticationHelper.class);

	private static SecureRandom random = new SecureRandom();

	static
	{
		try
		{
			Runnable task = new Runnable()
			{
				public void run()
				{
					long currentTime = System.currentTimeMillis();
					boolean deletedSomeEntry = false;

					do
					{
						synchronized (authSessions)
						{
							Set<String> keys = authSessions.keySet();

							for (String key : keys)
							{
								if (authSessions.get(key).validUntil < currentTime)
								{
									log.warn("Deleted Session info: - " + authSessions.get(key).toString());
									authSessions.remove(key);
									deletedSomeEntry = true;
									break;
								}
							}
						}
					}
					while (deletedSomeEntry);
				}
			};

			// BrokerExecutor.scheduleWithFixedDelay(task, 0, 5, TimeUnit.MINUTES);
		}
		catch (Exception e)
		{
			// This exception should never occur
		}
	}

	private static Map<String, ClientAuthenticationSessionInfo> authSessions = new HashMap<String, ClientAuthenticationSessionInfo>();

	private static class ClientAuthenticationSessionInfo
	{
		String communicationId;
		ClientAuthInfo clientAuthenticationInfo;
		long validUntil;
		byte[] challenge;
		SecretKey key;
		IoSession secureSession;
		IoSession regularSession;
		NetAuthentication.AuthMessageType nextExpectedMessage;

		public String toString()
		{
			return String.format("[ClientAuthenticationSessionInfo] communicationId: %s, ", communicationId);
		}
	}

	public static void handleAuthMessage(BrokerProtocolHandler brokerProtocolHandler, NetAuthentication auth)
	{
		switch (auth.getAuthMessageType())
		{
		case SERVER_CHALLENGE:
			handleServerChallenge(brokerProtocolHandler, auth);
			break;
		case CLIENT_CHALLENGE_RESPONSE:
			handleClientChallengeResponse(brokerProtocolHandler, auth);
			break;
		default:
			throw new RuntimeException("Invalid Authentication Messaage Type: " + auth.getAuthMessageType());
		}
	}

	private static IvParameterSpec ivParamSpec = new IvParameterSpec(new byte[16]);

	private static void handleServerChallenge(BrokerProtocolHandler brokerProtocolHandler, NetAuthentication auth)
	{
		System.out.println("ClientBrokerProtocolHandlerAuthenticationHelper.handleServerChallenge()");

		AuthServerChallenge authServerChallenge = auth.getAuthServerChallenge();

		String localCommunicationId = authServerChallenge.getLocalCommunicationId();

		SecureSessionInfo ssi = SecureSessionContainer.getInitializingSecureSessionInfo(localCommunicationId);
		if (ssi == null)
		{
			return;
			// TODO: decide what to do in this case
		}

		ssi.setCommunicationId(authServerChallenge.getCommunicationId());

		String secretType = authServerChallenge.getSecretType();
		ssi.setSecretType(secretType);

		SecretKey key = new SecretKeySpec(authServerChallenge.getSecret(), 0, authServerChallenge.getSecret().length, "AES");
		ssi.setKey(key);

		SecureSessionContainer.associateCommunicationId(localCommunicationId, authServerChallenge.getCommunicationId());

		byte[] challenge = authServerChallenge.getChallenge();

		String cipherAlg = "AES/CBC/NoPadding";

		Cipher cipher = null;
		try
		{
			cipher = Cipher.getInstance(cipherAlg);
			cipher.init(Cipher.ENCRYPT_MODE, key, ivParamSpec);
		}
		catch (Throwable t)
		{
			// This shouldn't happen.
			log.error("[ClientAuthenticationSessionInfo] initializing cipher", t);
			return;
		}

		byte[] newChallenge = new byte[8];
		random.nextBytes(newChallenge);
		ssi.setServerChallenge(newChallenge);

		byte[] data = ArrayUtils.addAll(challenge, newChallenge);

		byte[] protectedData = null;

		try
		{
			protectedData = cipher.doFinal(data);
		}
		catch (Throwable t)
		{
			log.error("[ClientAuthenticationSessionInfo] ciphering data", t);
			// This shouldn't happen.
			return;
		}

		ssi.setExpectedMessageType(AuthMessageType.CLIENT_CHALLENGE_RESPONSE);

		NetAuthentication.AuthServerChallengeResponseClientChallenge asccc = new NetAuthentication.AuthServerChallengeResponseClientChallenge(authServerChallenge.getCommunicationId(), protectedData);

		NetAuthentication netAuth = new NetAuthentication(AuthMessageType.SERVER_CHALLENGE_RESPONSE_CLIENT_CHALLENGE);
		netAuth.setAuthServerChallengeResponseClientChallenge(asccc);

		NetAction netAction = new NetAction(ActionType.AUTH);
		netAction.setAuthenticationMessage(netAuth);

		NetMessage msg = new NetMessage(netAction);

		try
		{
			brokerProtocolHandler.sendMessage(msg);
		}
		catch (Throwable e)
		{
			// TODO deal with this
			e.printStackTrace();
		}
	}

	private static void handleClientChallengeResponse(BrokerProtocolHandler brokerProtocolHandler, NetAuthentication auth)
	{
		System.out.println("ClientBrokerProtocolHandlerAuthenticationHelper.handleClientChallengeResponse()");

		
		AuthClientChallengeResponse accr = auth.getAuthClientChallengeResponse();

		String communicationId = accr.getCommunicationId();
		String localComId = SecureSessionContainer.getLocalCommunicationId(communicationId);

		SecureSessionInfo ssi = SecureSessionContainer.getInitializingSecureSessionInfo(localComId);
		if (ssi == null)
		{
			return;
			// TODO: decide what to do in this case
		}

		if (!Arrays.equals(accr.getChallenge(), ssi.getServerChallenge()))
		{
			// Server did't respond correctly
			// TODO: decide what to do in this case
			return;
		}

		ssi.setServerChallenge(null);
		ssi.setExpectedMessageType(null);
		ssi.setLocalCommunicationId(null);
		ssi.setSecretType(null);

		SecureSessionContainer.removeInitializingSecureSessionInfo(localComId, communicationId);

		ssi.getBrokerProtocolHandler().getBrokerClient().setSecureSessionInfo(ssi);

		AuthClientAcknowledge aca = new NetAuthentication.AuthClientAcknowledge(communicationId);
		NetAuthentication netAuthentication = new NetAuthentication(AuthMessageType.CLIENT_ACKNOWLEDGE);
		netAuthentication.setAuthClientAcknowledge(aca);
		NetAction netAction = new NetAction(ActionType.AUTH);
		netAction.setAuthenticationMessage(netAuthentication);

		NetMessage message = new NetMessage(netAction);

		try
		{
			brokerProtocolHandler.sendMessageOverSsl(message);
		}
		catch (Throwable t)
		{
			log.error("[ClientAuthenticationSessionInfo.handleClientChallengeResponse] failed to send message", t);
		}
	}

	private static void printArray(String desc, byte[] serverChallenge)
	{
		System.out.println(desc);
		for (byte b : serverChallenge)
			System.out.print(b);
		System.out.print(" ");

		System.out.println();
	}
}
