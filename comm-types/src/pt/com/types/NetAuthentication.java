package pt.com.types;

import java.util.List;

public class NetAuthentication
{

	public static class AuthClientAuthentication
	{
		private String authenticationType;
		private String localCommunicationId;
		private byte[] token;
		private String userId;
		private List<String> roles;

		public AuthClientAuthentication(byte[] token, String localCommunicationId)
		{
			this.token = token;
			this.localCommunicationId = localCommunicationId;
		}

		public void setAuthenticationType(String authenticationType)
		{
			this.authenticationType = authenticationType;
		}

		public String getAuthenticationType()
		{
			return authenticationType;
		}

		public byte[] getToken()
		{
			return token;
		}

		public void setUserId(String userId)
		{
			this.userId = userId;
		}

		public String getUserId()
		{
			return userId;
		}

		public void setRoles(List<String> roles)
		{
			this.roles = roles;
		}

		public List<String> getRoles()
		{
			return roles;
		}

		public String getLocalCommunicationId()
		{
			return localCommunicationId;
		}
	}

	public static class AuthServerChallenge
	{
		private byte[] challenge;
		private byte[] secret;
		private String communicationId;
		private String secretType;
		private String localCommunicationId;

		public AuthServerChallenge(byte[] challenge, byte[] secret, String communicationId, String localCommunicationId)
		{
			this.challenge = challenge;
			this.secret = secret;
			this.communicationId = communicationId;
			this.localCommunicationId = localCommunicationId;
		}

		public byte[] getChallenge()
		{
			return challenge;
		}

		public byte[] getSecret()
		{
			return secret;
		}

		public String getCommunicationId()
		{
			return communicationId;
		}

		public void setSecretType(String secretType)
		{
			this.secretType = secretType;
		}

		public String getSecretType()
		{
			return secretType;
		}

		public String getLocalCommunicationId()
		{
			return localCommunicationId;
		}
	}

	public static class AuthServerChallengeResponseClientChallenge
	{
		private String communicationId;
		private byte[] protectedChallenges;

		public AuthServerChallengeResponseClientChallenge(String communicationId, byte[] protectedChallenges)
		{
			this.communicationId = communicationId;
			this.protectedChallenges = protectedChallenges;
		}

		public String getCommunicationId()
		{
			return communicationId;
		}

		public byte[] getProtectedChallenges()
		{
			return protectedChallenges;
		}
	}

	public static class AuthClientChallengeResponse
	{
		private String communicationId;
		private byte[] challenge;

		public AuthClientChallengeResponse(String communicationId, byte[] challenge)
		{
			this.communicationId = communicationId;
			this.challenge = challenge;
		}

		public String getCommunicationId()
		{
			return communicationId;
		}

		public byte[] getChallenge()
		{
			return challenge;
		}
	}

	public static class AuthClientAcknowledge
	{
		private String communicationId;

		public AuthClientAcknowledge(String communicationId)
		{
			this.communicationId = communicationId;
		}

		public String getCommunicationId()
		{
			return communicationId;
		}
	}

	public enum AuthMessageType
	{
		CLIENT_AUTH, SERVER_CHALLENGE, SERVER_CHALLENGE_RESPONSE_CLIENT_CHALLENGE, CLIENT_CHALLENGE_RESPONSE, CLIENT_ACKNOWLEDGE
	}

	private AuthClientAuthentication authClientAuthrentication;
	private AuthServerChallenge authServerChallenge;
	private AuthServerChallengeResponseClientChallenge authServerChallengeResponseClientChallenge;
	private AuthClientChallengeResponse authClientChallengeResponse;
	private AuthClientAcknowledge authClientAcknowledge;

	private AuthMessageType authMessageType;

	public NetAuthentication(AuthMessageType authMessageType)
	{
		this.authMessageType = authMessageType;
	}

	public void setAuthClientAuthentication(AuthClientAuthentication authClientAuthrentication)
	{
		this.authClientAuthrentication = authClientAuthrentication;
	}

	public AuthClientAuthentication getAuthClientAuthentication()
	{
		return authClientAuthrentication;
	}

	public void setAuthServerChallenge(AuthServerChallenge authServerChallenge)
	{
		this.authServerChallenge = authServerChallenge;
	}

	public AuthServerChallenge getAuthServerChallenge()
	{
		return authServerChallenge;
	}

	public void setAuthServerChallengeResponseClientChallenge(AuthServerChallengeResponseClientChallenge authServerChallengeResponseClientChallenge)
	{
		this.authServerChallengeResponseClientChallenge = authServerChallengeResponseClientChallenge;
	}

	public AuthServerChallengeResponseClientChallenge getAuthServerChallengeResponseClientChallenge()
	{
		return authServerChallengeResponseClientChallenge;
	}

	public void setAuthClientChallengeResponse(AuthClientChallengeResponse authClientChallengeResponse)
	{
		this.authClientChallengeResponse = authClientChallengeResponse;
	}

	public AuthClientChallengeResponse getAuthClientChallengeResponse()
	{
		return authClientChallengeResponse;
	}

	public void setAuthClientAcknowledge(AuthClientAcknowledge authClientAcknowledge)
	{
		this.authClientAcknowledge = authClientAcknowledge;
	}

	public AuthClientAcknowledge getAuthClientAcknowledge()
	{
		return authClientAcknowledge;
	}

	public AuthMessageType getAuthMessageType()
	{
		return authMessageType;
	}

}
