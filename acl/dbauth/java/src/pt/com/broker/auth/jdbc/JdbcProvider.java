package pt.com.broker.auth.jdbc;

import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.auth.AuthInfo;
import pt.com.broker.auth.CredentialsProvider;
import pt.com.broker.auth.ProviderInfo;

/**
 * JdbcProvider implements a credentials provider for database based access.
 * 
 */
public class JdbcProvider implements CredentialsProvider
{
	private static final Logger log = LoggerFactory.getLogger(JdbcProvider.class);

	private final String providerName = "BrokerRolesDB";

	private final String username;
	private final String password;
	
	public JdbcProvider(String username, String password)
	{
		this.username = username;
		this.password = password;
	}

	@Override
	public AuthInfo getCredentials() throws Exception
	{
		AuthInfo aui = new AuthInfo(username, null, password.getBytes(Charset.forName("UTF-8")), providerName);
		return aui;
	}

	@Override
	public boolean init(ProviderInfo info)
	{
		return true;
	}

	@Override
	public String getAuthenticationType()
	{
		return providerName;
	}

	@Override
	public String toString() {
		return "JdbcProvider [providerName=" + providerName + ", username=" + username + ", password=" + password + "]";
	}
}