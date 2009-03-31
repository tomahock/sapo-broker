package pt.com.broker.security;

import javax.crypto.SecretKey;

import pt.com.broker.client.BrokerProtocolHandler;
import pt.com.common.security.ClientAuthInfo;
import pt.com.common.security.authentication.AuthenticationCredentialsProvider;
import pt.com.types.NetAuthentication.AuthMessageType;

public class SecureSessionInfo
{
	private String localCommunicationId;
		
	private AuthenticationCredentialsProvider authProvider;
	private ClientAuthInfo userCredentials;
	private ClientAuthInfo providerCredentials;
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

	public void setUserCredentials(ClientAuthInfo userCredentials)
	{
		this.userCredentials = userCredentials;
	}

	public ClientAuthInfo getUserCredentials()
	{
		return userCredentials;
	}

	public void setProviderCredentials(ClientAuthInfo providerCredentials)
	{
		this.providerCredentials = providerCredentials;
	}

	public ClientAuthInfo getProviderCredentials()
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

	public void setAuthProvider(AuthenticationCredentialsProvider authProvider)
	{
		this.authProvider = authProvider;
	}

	public AuthenticationCredentialsProvider getAuthProvider()
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

	public void setExpectedMessageType(AuthMessageType expectedMessageType)
	{
		this.expectedMessageType = expectedMessageType;
	}

	public AuthMessageType getExpectedMessageType()
	{
		return expectedMessageType;
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
