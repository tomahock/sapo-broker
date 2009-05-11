package pt.com.types;

import java.util.List;

public class NetAuthentication
{

	private String authenticationType;
	private byte[] token;
	private String userId;
	private List<String> roles;

	public NetAuthentication(byte[] token)
	{
		this.token = token;
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
}
