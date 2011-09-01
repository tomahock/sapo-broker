package pt.com.broker.auth;

import java.nio.charset.Charset;
import java.util.List;

/**
 * AuthInfo represents client's authentication information.
 * 
 */

public class AuthInfo
{
	private String userId;
	private List<String> roles;
	private byte[] token;
	private CredentialsProvider credentialProvider;
	private final String authenticationType;

	/**
	 * Creates an AuthInfo instance.
	 * 
	 * @param userId
	 *            User identification, such as an username.
	 * @param password
	 *            User password. This is transformed in a binary token using UTF-8.
	 * @param credentialProvider
	 *            Credentials provider being used (e.g., SapoSTSProvider).
	 */
	public AuthInfo(String userId, String password, CredentialsProvider credentialProvider)
	{
		this(userId, null, password.getBytes(Charset.forName("UTF-8")), credentialProvider);
	}
	
	/**
	 * Creates an AuthInfo instance.
	 * 
	 * @param userId
	 *            User identification, such as an username.
	 * @param password
	 *            User password. This is transformed in a binary token using UTF-8.
	 * @param authenticationType
	 *            The type of authentication being used (e.g., BrokerRolesDB).
	 */
	public AuthInfo(String userId, String password, String authenticationType)
	{
		this(userId, null, password.getBytes(Charset.forName("UTF-8")), authenticationType, null);
	}

	/**
	 * Creates an AuthInfo instance.
	 * 
	 * @param userId
	 *            User identification, such as an username.
	 * @param roles
	 *            User roles associated with the roles.
	 * @param token
	 *            User binary authentication token.
	 * @param credentialProvider
	 *             Credentials provider being used (e.g., SapoSTSProvider).
	 */
	public AuthInfo(String userId, List<String> roles, byte[] token, CredentialsProvider credentialProvider)
	{
		this(userId, roles, token, credentialProvider.getAuthenticationType(), credentialProvider);
	}

	public AuthInfo(String userId, List<String> roles, byte[] token, String authenticationType) {
		this(userId, roles, token, authenticationType, null);
	}
	
	
	public AuthInfo(String userId, List<String> roles, byte[] token, String authenticationType, CredentialsProvider credentialProvider)
	{
		this.userId = userId;
		this.roles = roles;
		this.token = token;
		this.authenticationType = authenticationType;
		this.credentialProvider = credentialProvider;
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

	public void setToken(byte[] token)
	{
		this.token = token;
	}

	public byte[] getToken()
	{
		return token;
	}

	public String getUserAuthenticationType()
	{
		return authenticationType;
	}

	public CredentialsProvider getCredentialProvider() {
		return credentialProvider;
	}

	@Override
	public String toString() {
		return "AuthInfo [userId=" + userId + ", credentialProvider=" + credentialProvider + "]";
	}
	
	
}
