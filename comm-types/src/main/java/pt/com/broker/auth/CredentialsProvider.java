package pt.com.broker.auth;

/**
 * CredentialsProvider should be implemented by types providing credentials, that is, given a set of user information they obtain other type of authentication information (e.g., transform an username-password into a service token).
 * 
 */

public interface CredentialsProvider
{
	AuthInfo getCredentials() throws Exception;

	boolean init(ProviderInfo info);

	String getAuthenticationType();
}
