package pt.com.common.security.authentication;

import java.util.Map;
import java.util.TreeMap;


public class AuthenticationCredentialsProviderFactory
{
	private static Map<String, AuthenticationCredentialsProvider> validators;

	static
	{
			validators = new TreeMap<String, AuthenticationCredentialsProvider>();
			
			validators.put("SapoSTS", new SapoSTSAuthenticationCredentialsProvider() );
	}

	public static AuthenticationCredentialsProvider getDefaultProvider()
	{
		return getProvider(null);
	}

	public static AuthenticationCredentialsProvider getProvider(String validationType)
	{
		if(validationType== null)
			validationType = "SapoSTS";
		
		return validators.get(validationType);
	}
}
