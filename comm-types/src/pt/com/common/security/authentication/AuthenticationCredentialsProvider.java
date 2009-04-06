package pt.com.common.security.authentication;

import pt.com.common.security.ClientAuthInfo;

public interface AuthenticationCredentialsProvider
{
	ClientAuthInfo getCredentials(ClientAuthInfo clientAuthInfo) throws Exception;
	void init();
}
