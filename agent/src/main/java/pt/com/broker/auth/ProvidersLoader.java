package pt.com.broker.auth;

import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.gcs.conf.GlobalConfig;

/**
 * ProvidersLoader is responsible for loading available authentication providers.
 * 
 */

public class ProvidersLoader
{
	private static Logger log = LoggerFactory.getLogger(ProvidersLoader.class);

	public static void init()
	{
		loadCredentialValidators();
	}

	private static void loadCredentialValidators()
	{
		Map<String, ProviderInfo> credentialValidatorProviders = GlobalConfig.getCredentialValidatorProviders();

		synchronized (credentialValidatorProviders)
		{
			Set<String> provSet = credentialValidatorProviders.keySet();
			for (String prov : provSet)
			{
				try
				{
					ProviderInfo providerInfo = credentialValidatorProviders.get(prov);
					Class<?> provClass = Class.forName(providerInfo.getClassName());
					AuthInfoValidator validatorProv = (AuthInfoValidator) provClass.newInstance();

					if (validatorProv.init(providerInfo))
					{
						AuthInfoVerifierFactory.addValidator(providerInfo.getName(), validatorProv);
						log.info(providerInfo.getName() + " credentials validator loaded.");
					}
					else
					{
						log.error("Failed to initialize Credentials Provider: " + providerInfo.getName());
					}
				}
				catch (Exception e)
				{
					log.error("Failed to load an authentication provider", e);
				}
			}
		}
	}
}
