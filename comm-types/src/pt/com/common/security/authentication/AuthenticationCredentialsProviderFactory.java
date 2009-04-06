package pt.com.common.security.authentication;

import java.util.Map;
import java.util.TreeMap;


public class AuthenticationCredentialsProviderFactory
{
	private static Map<String, AuthenticationCredentialsProvider> validators;

	static
	{
			validators = new TreeMap<String, AuthenticationCredentialsProvider>();
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

	public static void addProvider(String validationType, AuthenticationCredentialsProvider provider)
	{
		validators.put(validationType, provider);
	}
}
