package pt.com.broker.auth;

import java.util.Map;
import java.util.TreeMap;

import pt.com.broker.auth.AuthInfoValidator;

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
