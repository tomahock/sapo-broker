package pt.com.broker.security;

import javax.crypto.SecretKey;

import pt.com.broker.auth.AuthInfo;
import pt.com.broker.auth.CredentialsProvider;
import pt.com.broker.client.BrokerProtocolHandler;
import pt.com.broker.codec.thrift.AuthMessageType;

public class SecureSessionInfo
{
	private String localCommunicationId;

	private CredentialsProvider authProvider;
	private AuthInfo userCredentials;
	private AuthInfo providerCredentials;
	private BrokerProtocolHandler brokerProtocolHandler;

	private String communicationId;
	private SecretKey key;
	private String secretType;
	private byte[] serverChallenge;
	private AuthMessageType expectedMessageType;

	public void setLocalCommunicationId(String localCommunicationId)
	{
		this.localCommunicationId = localCommunicationId;
		System.currentTimeMillis();
	}

	public String getLocalCommunicationId()
	{
		return localCommunicationId;
	}

	public void setUserCredentials(AuthInfo userCredentials)
	{
		this.userCredentials = userCredentials;
	}

	public AuthInfo getUserCredentials()
	{
		return userCredentials;
	}

	public void setProviderCredentials(AuthInfo providerCredentials)
	{
		this.providerCredentials = providerCredentials;
	}

	public AuthInfo getProviderCredentials()
	{
		return providerCredentials;
	}

	public void setBrokerProtocolHandler(BrokerProtocolHandler brokerProtocolHandler)
	{
		this.brokerProtocolHandler = brokerProtocolHandler;
	}

	public BrokerProtocolHandler getBrokerProtocolHandler()
	{
		return brokerProtocolHandler;
	}

	public void setAuthProvider(CredentialsProvider authProvider)
	{
		this.authProvider = authProvider;
	}

	public CredentialsProvider getAuthProvider()
	{
		return authProvider;
	}

	public void setCommunicationId(String communicationId)
	{
		this.communicationId = communicationId;
	}

	public String getCommunicationId()
	{
		return communicationId;
	}

	public void setKey(SecretKey key)
	{
		this.key = key;
	}

	public SecretKey getKey()
	{
		return key;
	}

	public void setSecretType(String secretType)
	{
		this.secretType = secretType;
	}

	public String getSecretType()
	{
		return secretType;
	}

	public void setServerChallenge(byte[] serverChallenge)
	{
		this.serverChallenge = serverChallenge;
	}

	public byte[] getServerChallenge()
	{
		return serverChallenge;
	}

}
