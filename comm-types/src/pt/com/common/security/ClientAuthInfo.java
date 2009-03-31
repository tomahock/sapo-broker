package pt.com.common.security;

import java.util.List;

public class ClientAuthInfo {
	private String userId;
	private List<String> roles;
	private byte[] token;
	private String userAuthenticationType;
	private String password;
	
	public ClientAuthInfo(String userId, String password)
	{
		this(userId, null, null, null, password);
	}
	
	public ClientAuthInfo(String userId, List<String> roles, byte[] token, String userAuthenticationType, String password)
	{
		this.userId = userId;
		this.roles = roles;
		this.token = token;
		this.userAuthenticationType = userAuthenticationType;
		this.password = password;
	}
	
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getUserId() {
		return userId;
	}
	public String getPassword() {
		return password;
	}
	public void setRoles(List<String> roles) {
		this.roles = roles;
	}
	public List<String> getRoles() {
		return roles;
	}
	public void setToken(byte[] token) {
		this.token = token;
	}
	public byte[] getToken() {
		return token;
	}
	public void setUserAuthenticationType(String userAuthenticationType) {
		this.userAuthenticationType = userAuthenticationType;
	}
	public String getUserAuthenticationType() {
		return userAuthenticationType;
	}
}
