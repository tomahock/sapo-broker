package pt.com.broker.auth;

import java.util.Map;
import java.util.TreeMap;

/**
 * AuthInfoVerifierFactory stores available AuthInfoValidator objects and retrives them by name.
 * 
 */

public class AuthInfoVerifierFactory
{
	private static Map<String, AuthInfoValidator> validators;

	static
	{
		validators = new TreeMap<String, AuthInfoValidator>();
	}

	public static AuthInfoValidator getValidator(String validationType)
	{
		return validators.get(validationType);
	}

	public static void addValidator(String validationType, AuthInfoValidator validator)
	{
		validators.put(validationType, validator);
	}

}
