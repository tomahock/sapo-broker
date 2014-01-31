package pt.com.broker.auth;

import java.util.Map;
import java.util.TreeMap;

/**
 * CredentialsProviderFactory is a factory class intended to store and retrieve credential providers.
 * 
 */

public class CredentialsProviderFactory
{
	private static Map<String, CredentialsProvider> validators;

	static
	{
		validators = new TreeMap<String, CredentialsProvider>();
	}

	public static CredentialsProvider getDefaultProvider()
	{
		return getProvider(null);
	}

	public static CredentialsProvider getProvider(String validationType)
	{
		if (validationType == null)
			validationType = "SapoSTS";

		return validators.get(validationType);

	}

	public static void addProvider(String validationType, CredentialsProvider provider)
	{
		validators.put(validationType, provider);
	}
}
