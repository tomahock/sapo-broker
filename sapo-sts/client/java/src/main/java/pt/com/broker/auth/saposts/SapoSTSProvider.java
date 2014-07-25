package pt.com.broker.auth.saposts;

import org.apache.commons.lang3.StringUtils;
import pt.com.broker.auth.AuthInfo;
import pt.com.broker.auth.CredentialsProvider;
import pt.com.broker.auth.ProviderInfo;

import java.nio.charset.Charset;

/**
 * SapoSTSProvider implements a credentials provider for Sapo STS.
 * 
 */
public class SapoSTSProvider implements CredentialsProvider
{
	private final String providerName = "SapoSTS";

	private final String username;
	private final String password;
	private final String stsLocation;

	public SapoSTSProvider(String username, String password)
	{
		this(username, password, SAPOStsToken.DEFAULT_BASE_URL);
	}

	public SapoSTSProvider(String username, String password, String stsLocation)
	{
		if (StringUtils.isBlank(stsLocation))
		{
			throw new IllegalArgumentException("STS Location URL must not be blank");
		}
		this.username = username;
		this.password = password;
		this.stsLocation = stsLocation;
	}

	@Override
	public AuthInfo getCredentials() throws Exception
	{
		String strToken = SAPOStsToken.getToken(username, password);
		byte[] token = null;
		token = strToken.getBytes(Charset.forName("UTF-8"));

		AuthInfo aui = new AuthInfo(username, null, token, providerName);
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

	public String getStsLocation()
	{
		return stsLocation;
	}

	@Override
	public String toString()
	{
		return "SapoSTSProvider [providerName=" + providerName + ", stsLocation=" + stsLocation + "]";
	}
}