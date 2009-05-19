package pt.com.broker.auth;

public interface CredentialsProvider
{
	AuthInfo getCredentials(AuthInfo clientAuthInfo) throws Exception;

	void init();

	String getAuthenticationType();
}
