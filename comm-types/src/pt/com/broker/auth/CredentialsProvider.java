package pt.com.broker.auth;

public interface CredentialsProvider
{
	AuthInfo getCredentials(AuthInfo clientAuthInfo) throws Exception;

	boolean init(ProviderInfo info);

	String getAuthenticationType();
}
