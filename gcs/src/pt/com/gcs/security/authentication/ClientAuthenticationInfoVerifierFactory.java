package pt.com.broker.security.authentication;

import java.util.Map;
import java.util.TreeMap;

import pt.com.common.security.ClientAuthenticationInfoValidator;

public class ClientAuthenticationInfoVerifierFactory
{
	private static Map<String, ClientAuthenticationInfoValidator> validators;

	static
	{
			validators = new TreeMap<String, ClientAuthenticationInfoValidator>();
			
			validators.put("SapoSTS", new SapoSTSAuthenticationInfoValidator() );
	}

	public static ClientAuthenticationInfoValidator getDefaultValidator()
	{
		return getValidator(null);
	}

	public static ClientAuthenticationInfoValidator getValidator(String validationType)
	{
		if(validationType== null)
			validationType = "SapoSTS";
		
		return validators.get(validationType);
	}

}
