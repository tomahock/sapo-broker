package pt.com.broker.net;

import java.net.InetSocketAddress;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import org.apache.mina.core.session.IoSession;
import org.caudexorigo.concurrent.Sleep;
import org.caudexorigo.text.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.core.BrokerExecutor;
import pt.com.broker.security.Session;
import pt.com.broker.security.SessionProperties;
import pt.com.broker.security.authentication.ClientAuthenticationInfoVerifierFactory;
import pt.com.common.security.AuthenticationFailException;
import pt.com.common.security.ClientAuthInfo;
import pt.com.common.security.ClientAuthenticationInfoValidationResult;
import pt.com.common.security.ClientAuthenticationInfoValidator;
import pt.com.gcs.conf.GcsInfo;
import pt.com.types.NetAction;
import pt.com.types.NetAuthentication;
import pt.com.types.NetMessage;
import pt.com.types.NetAuthentication.AuthClientAcknowledge;
import pt.com.types.NetAuthentication.AuthClientAuthentication;
import pt.com.types.NetAuthentication.AuthMessageType;
import pt.com.types.NetAuthentication.AuthServerChallengeResponseClientChallenge;

public class BrokerProtocolHandlerAuthenticationHelper
{
	private static final int MAX_WRITE_BUFFER_SIZE = 5000;
	private static final short CHALLENGE_SIZE_IN_BYTES = 8;

	private static final Logger log = LoggerFactory.getLogger(BrokerProtocolHandlerAuthenticationHelper.class);

	private static SecureRandom random = new SecureRandom();
	private static KeyGenerator keyGenerator;

	static
	{
		try
		{
			keyGenerator = KeyGenerator.getInstance("AES");
			keyGenerator.init(128);

			Runnable task = new Runnable()
			{
				public void run()
				{
					long currentTime = System.currentTimeMillis();

					boolean deletedSomeEntry;
					do
					{
						deletedSomeEntry = false;
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

			BrokerExecutor.scheduleWithFixedDelay(task, 0, 5, TimeUnit.MINUTES);
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

	public static void handleAuthMessage(IoSession session, NetMessage request)
	{
		NetAuthentication auth = request.getAction().getAuthorizationMessage();
		if (!validateAuthenticationMessageConnection(session, auth))
		{
			throw new RuntimeException("Invalid channel used for authentication");
		}
		switch (auth.getAuthMessageType())
		{
		case CLIENT_AUTH:
			handleClientAuthentication(session, auth);
			break;
		case SERVER_CHALLENGE_RESPONSE_CLIENT_CHALLENGE:
			handleServerChallengeResponseClientChallenge(session, auth);
			break;
		case CLIENT_ACKNOWLEDGE:
			handleClientAcknowledge(session, auth);
			break;
		default:
			throw new RuntimeException("Invalid Authentication Messaage Type: " + auth.getAuthMessageType());
		}
	}

	private static void handleClientAuthentication(IoSession session, NetAuthentication request)
	{
		AuthClientAuthentication authClientAuthrentication = request.getAuthClientAuthentication();

		// Validate client credentials
		ClientAuthInfo info = new ClientAuthInfo(authClientAuthrentication.getUserId(), authClientAuthrentication.getRoles(), authClientAuthrentication.getToken(), authClientAuthrentication.getAuthenticationType(), null);
		ClientAuthenticationInfoValidator validator = ClientAuthenticationInfoVerifierFactory.getValidator(info.getUserAuthenticationType());
		ClientAuthenticationInfoValidationResult validate;
		try
		{
			validate = validator.validate(info);
		}
		catch (Exception e)
		{
			throw new AuthenticationFailException("Internal Error", e);
		}

		if (!validate.areCredentialsValid())
			throw new AuthenticationFailException(validate.getReasonForFailure());

		info.setRoles(validate.getRoles());

		String communicationIdStr = RandomStringUtils.random(16);

		// Build session info
		ClientAuthenticationSessionInfo clientSessionInfo = new ClientAuthenticationSessionInfo();
		clientSessionInfo.communicationId = communicationIdStr;
		clientSessionInfo.clientAuthenticationInfo = info;
		clientSessionInfo.validUntil = System.currentTimeMillis() + (1000 * 60 * 5);
		clientSessionInfo.challenge = new byte[CHALLENGE_SIZE_IN_BYTES];
		random.nextBytes(clientSessionInfo.challenge);
		clientSessionInfo.key = keyGenerator.generateKey();
		clientSessionInfo.secureSession = session;
		clientSessionInfo.nextExpectedMessage = NetAuthentication.AuthMessageType.SERVER_CHALLENGE_RESPONSE_CLIENT_CHALLENGE;

		synchronized (authSessions)
		{
			authSessions.put(communicationIdStr, clientSessionInfo);
		}

		// Respond to client
		NetAuthentication netAuthentication = new NetAuthentication(NetAuthentication.AuthMessageType.SERVER_CHALLENGE);
		NetAuthentication.AuthServerChallenge serverChallenge = new NetAuthentication.AuthServerChallenge(clientSessionInfo.challenge, clientSessionInfo.key.getEncoded(), clientSessionInfo.communicationId, authClientAuthrentication.getLocalCommunicationId());
		serverChallenge.setSecretType("AES128");

		netAuthentication.setAuthServerChallenge(serverChallenge);

		sendMessage(session, netAuthentication);
	}

	static IvParameterSpec ivParamSpec = new IvParameterSpec(new byte[16]);

	private static void handleServerChallengeResponseClientChallenge(IoSession session, NetAuthentication request)
	{
		AuthServerChallengeResponseClientChallenge ascrcc = request.getAuthServerChallengeResponseClientChallenge();

		ClientAuthenticationSessionInfo clientSessionInfo;
		synchronized (authSessions)
		{
			clientSessionInfo = authSessions.get(ascrcc.getCommunicationId());
		}

		if (clientSessionInfo == null)
			throw new AuthenticationFailException("Protocol uninitilized.");

		if (!validateAuthenticationMessageSequence(request, clientSessionInfo))
		{
			throw new RuntimeException("Invalid message sequence.");
		}

		byte[] protChallenges = ascrcc.getProtectedChallenges();

		String cipherAlg = "AES/CBC/NoPadding";

		Cipher cipher = null;
		try
		{
			cipher = Cipher.getInstance(cipherAlg);
			cipher.init(Cipher.DECRYPT_MODE, clientSessionInfo.key, ivParamSpec);
		}
		catch (Exception e)
		{
			// This shouldn't happen.
			System.out.println(e);
			return;
		}

		byte[] unprotChallenges = null;

		try
		{
			unprotChallenges = cipher.doFinal(protChallenges);
		}
		catch (IllegalBlockSizeException e)
		{
			throw new AuthenticationFailException("Invalid protected challanges size.");
		}
		catch (BadPaddingException e)
		{
			throw new AuthenticationFailException("Invalid protected challanges padding");
		}

		if (unprotChallenges.length < (CHALLENGE_SIZE_IN_BYTES * 2))
			throw new AuthenticationFailException("Invalid unprotected challanges size");

		byte[] serverChallenge = Arrays.copyOfRange(unprotChallenges, 0, CHALLENGE_SIZE_IN_BYTES);
		byte[] clientChallenge = Arrays.copyOfRange(unprotChallenges, CHALLENGE_SIZE_IN_BYTES, (CHALLENGE_SIZE_IN_BYTES * 2));

		if (!Arrays.equals(serverChallenge, clientSessionInfo.challenge))
			throw new AuthenticationFailException("Invalid challange response!");

		NetAuthentication.AuthClientChallengeResponse auccr = new NetAuthentication.AuthClientChallengeResponse(clientSessionInfo.communicationId, clientChallenge);

		NetAuthentication netAuthentication = new NetAuthentication(AuthMessageType.CLIENT_CHALLENGE_RESPONSE);
		netAuthentication.setAuthClientChallengeResponse(auccr);

		clientSessionInfo.regularSession = session;

		clientSessionInfo.nextExpectedMessage = AuthMessageType.CLIENT_ACKNOWLEDGE;

		sendMessage(clientSessionInfo.secureSession, netAuthentication);

	}

	private static void handleClientAcknowledge(IoSession session, NetAuthentication request)
	{
		AuthClientAcknowledge clientAck = request.getAuthClientAcknowledge();

		ClientAuthenticationSessionInfo clientSessionInfo;
		synchronized (authSessions)
		{
			clientSessionInfo = authSessions.get(clientAck.getCommunicationId());
		}

		if (clientSessionInfo == null)
			throw new AuthenticationFailException("Protocol uninitilized.");

		if (!validateAuthenticationMessageSequence(request, clientSessionInfo))
		{
			throw new RuntimeException("Invalid message sequence.");
		}

		synchronized (authSessions)
		{
			authSessions.remove(clientSessionInfo);
		}

		Session plainSession = (Session) clientSessionInfo.regularSession.getAttribute("BROKER_SESSION_PROPERTIES");
		Session protectedSession = (Session) clientSessionInfo.secureSession.getAttribute("BROKER_SESSION_PROPERTIES");

		SessionProperties plainSessionProps = plainSession.getSessionProperties();
		SessionProperties sslSessionProps = protectedSession.getSessionProperties();

		plainSessionProps.setRoles(clientSessionInfo.clientAuthenticationInfo.getRoles());
		sslSessionProps.setRoles(clientSessionInfo.clientAuthenticationInfo.getRoles());

		plainSession.updateAcl();
		protectedSession.updateAcl();

		plainSessionProps.setKey(clientSessionInfo.key);
		sslSessionProps.setKey(clientSessionInfo.key);
	}

	private static void sendMessage(final IoSession ios, NetAuthentication authMessage)
	{
		NetAction action = new NetAction(NetAction.ActionType.AUTH);
		action.setAuthenticationMessage(authMessage);
		NetMessage message = new NetMessage(action, null);

		ios.write(message);

		boolean isSuspended = (Boolean) ios.getAttribute("IS_SUSPENDED", Boolean.FALSE);

		if ((ios.getScheduledWriteMessages() > MAX_WRITE_BUFFER_SIZE) && !isSuspended)
		{
			ios.suspendRead();
			ios.setAttribute("IS_SUSPENDED", Boolean.TRUE);

			Runnable resumer = new Runnable()
			{
				public void run()
				{
					int counter = 0;
					while (true)
					{
						Sleep.time(5);
						counter++;
						if (ios.getScheduledWriteMessages() <= MAX_WRITE_BUFFER_SIZE)
						{
							ios.resumeRead();
							ios.setAttribute("IS_SUSPENDED", Boolean.FALSE);
							return;
						}
						if (counter % 1000 == 0)
						{
							log.warn("Client is slow to read ack messages.");
						}
					}
				}
			};
			BrokerExecutor.execute(resumer);
		}

	}

	private static boolean validateAuthenticationMessageSequence(NetAuthentication auth, ClientAuthenticationSessionInfo sessionInfo)
	{
		return auth.getAuthMessageType().equals(sessionInfo.nextExpectedMessage);
	}

	private static boolean validateAuthenticationMessageConnection(IoSession session, NetAuthentication auth)
	{
		InetSocketAddress sockAddress = (InetSocketAddress) session.getLocalAddress();

		switch (auth.getAuthMessageType())
		{
		case CLIENT_AUTH:
		case CLIENT_ACKNOWLEDGE:
			if (sockAddress.getPort() != GcsInfo.getBrokerSSLPort())
				return false;
		}
		return true;
	}

	private static void printArray(String desc, byte[] serverChallenge)
	{
		System.out.println(desc);
		for (byte b : serverChallenge)
		{
			System.out.print(b);
			System.out.print(" ");
		}

		System.out.println();
	}
}
