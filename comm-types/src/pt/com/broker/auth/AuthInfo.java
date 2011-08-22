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
	private String userAuthenticationType;

	/**
	 * Creates an AuthInfo instance.
	 * 
	 * @param userId
	 *            User identification, such as an username.
	 * @param password
	 *            User password. This is transformed in a binary token using UTF-8.
	 */
	public AuthInfo(String userId, String password)
	{
		this(userId, null, password.getBytes(Charset.forName("UTF-8")), null);
	}
	
	/**
	 * Creates an AuthInfo instance.
	 * 
	 * @param userId
	 *            User identification, such as an username.
	 * @param password
	 *            User password. This is transformed in a binary token using UTF-8.
	 * @param userAuthenticationType
	 *            The type of authentication being used (e.g., SapoSTS).
	 */
	public AuthInfo(String userId, String password, String userAuthenticationType)
	{
		this(userId, null, password.getBytes(Charset.forName("UTF-8")), userAuthenticationType);
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
	 * @param userAuthenticationType
	 *            The type of authentication being used (e.g., SapoSTS).
	 */
	public AuthInfo(String userId, List<String> roles, byte[] token, String userAuthenticationType)
	{
		this.userId = userId;
		this.roles = roles;
		this.token = token;
		this.userAuthenticationType = userAuthenticationType;
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

	public void setUserAuthenticationType(String userAuthenticationType)
	{
		this.userAuthenticationType = userAuthenticationType;
	}

	public String getUserAuthenticationType()
	{
		return userAuthenticationType;
	}
}
