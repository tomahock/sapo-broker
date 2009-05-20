package pt.com.broker.auth;

import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.auth.AuthInfoValidator;
import pt.com.broker.auth.CredentialsProvider;
import pt.com.broker.auth.CredentialsProviderFactory;
import pt.com.gcs.conf.GlobalConfig;
import pt.com.gcs.conf.ProviderInfo;

public class ProvidersLoader
{
	private static Logger log = LoggerFactory.getLogger(ProvidersLoader.class);

	public static void init()
	{
		// Loading order matters! Validators may use authentication providers
		loadAuthenticationProviders();
		loadCredentialValidators();
	}

	private static void loadAuthenticationProviders()
	{
		Map<String, ProviderInfo> authenticationProviders = GlobalConfig.getAuthenticationProviders();

		synchronized (authenticationProviders)
		{
			Set<String> provSet = authenticationProviders.keySet();
			for (String prov : provSet)
			{
				try
				{
					ProviderInfo providerInfo = authenticationProviders.get(prov);
					Class<?> provClass = Class.forName(providerInfo.getClassName());
					CredentialsProvider authProv = (CredentialsProvider) provClass.newInstance();
					authProv.init();

					CredentialsProviderFactory.addProvider(providerInfo.getName(), authProv);
				}
				catch (Exception e)
				{
					log.error("Failed to load an authentication provider", e);
				}
			}
		}

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

					validatorProv.init();
					AuthInfoVerifierFactory.addValidator(providerInfo.getName(), validatorProv);
				}
				catch (Exception e)
				{
					log.error("Failed to load an authentication provider", e);
				}
			}
		}
	}
}
