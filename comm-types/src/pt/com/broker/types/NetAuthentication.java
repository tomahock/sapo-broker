package pt.com.broker.types;

import java.util.List;

/**
 * Represents an Authentication message.
 * 
 */

public class NetAuthentication
{
	private String actionId;
	private String authenticationType;
	private byte[] token;
	private String userId;
	private List<String> roles;

	/**
	 * Initializes a NetAuthentication instance.
	 * 
	 * @param token
	 *            Can represent a password or some binary token. If the original value is text then it should be encoded in UTF-8.
	 */
	public NetAuthentication(byte[] token, String authenticationType)
	{
		this.token = token;
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

	public void setActionId(String actionId)
	{
		this.actionId = actionId;
	}

	public String getActionId()
	{
		return actionId;
	}
}
