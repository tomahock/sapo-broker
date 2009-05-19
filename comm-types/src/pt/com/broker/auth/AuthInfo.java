package pt.com.broker.auth;

import java.util.List;

public class AuthInfo
{
	private String userId;
	private List<String> roles;
	private byte[] token;
	private String userAuthenticationType;

	public AuthInfo(String userId, String password)
	{
		this(userId, null, password.getBytes(), null);
	}

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
