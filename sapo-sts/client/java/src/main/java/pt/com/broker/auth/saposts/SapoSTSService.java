package pt.com.broker.auth.saposts;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.caudexorigo.concurrent.CustomExecutors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import pt.com.broker.auth.AuthInfo;
import pt.com.broker.auth.CredentialsProvider;
import pt.com.broker.auth.ProviderInfo;
import pt.com.broker.auth.saposts.SapoSTSParameterProvider.Parameters;
import pt.com.broker.auth.saposts.utils.CredentialObfuscation;

/**
 * SapoSTSService is responsible for managing agent's credentials (STS Token) to Sapo STS and automatically renew it.
 * 
 */

public class SapoSTSService
{
	private static final Logger log = LoggerFactory.getLogger(SapoSTSService.class);
	private static final ScheduledThreadPoolExecutor exec_srv = CustomExecutors.newScheduledThreadPool(1, "SapoSTS Authentication Sevice");

	private static AuthInfo agentAuthenticationInfo = null;
	private static CredentialsProvider authProvider = null;

	public static boolean start(ProviderInfo providerInfo)
	{
		if (providerInfo == null)
		{
			log.error("Failed to obtain Authentication information from GcsInfo");
			return false;
		}

		Element confNode = providerInfo.getParameters();

		if (confNode == null)
		{
			log.error("There is no configuration info regarding SapoSTS authentication provider.");
			return false;
		}

		NodeList stsNode = confNode.getElementsByTagName("sts");
		if (stsNode.getLength() == 0)
		{
			log.error("Missing 'sts' element in validation provider params");
			return false;
		}

		Element stsElem = (Element) stsNode.item(0);

		String stsLocation = null;
		String stsUsername = null;
		String stsPassword = null;

		NodeList locationElems = stsElem.getElementsByTagName("sts-location");
		if (locationElems.getLength() == 0)
		{
			log.error("Missing sts-location info");
			return false;
		}
		stsLocation = locationElems.item(0).getTextContent();

		NodeList usernameElems = stsElem.getElementsByTagName("sts-username");
		if (usernameElems.getLength() == 0)
		{
			log.error("Missing sts-username info");
			return false;
		}
		String encodedStsUsername = usernameElems.item(0).getTextContent();
		stsUsername = CredentialObfuscation.deObfuscate(encodedStsUsername);

		NodeList passwordElems = stsElem.getElementsByTagName("sts-password");
		if (passwordElems.getLength() == 0)
		{
			log.error("Missing sts-password info");
			return false;
		}
		String encodedStsPassword = passwordElems.item(0).getTextContent();
		stsPassword = CredentialObfuscation.deObfuscate(encodedStsPassword);

		Parameters parameters = new SapoSTSParameterProvider.Parameters(stsLocation, stsUsername, stsPassword);
		SapoSTSParameterProvider.setSTSParameters(parameters);

		authProvider = new SapoSTSProvider(stsUsername, stsPassword, stsLocation);
		authProvider.init(providerInfo);

		try
		{
			agentAuthenticationInfo = authProvider.getCredentials();
			if (agentAuthenticationInfo == null)
			{
				log.error("Failed to get credentials. Provider Info: " + authProvider);
				return false;
			}
		}
		catch (Exception e)
		{
			log.warn(String.format("Failed to get credentials for Service BUS. Reason: '%s'. ProviderInfo: %s", e.getMessage(), authProvider));
			return false;
		}

		Runnable renew = new Runnable()
		{
			public void run()
			{
				renewCredentials();
			}
		};

		log.info("STS Credentials obtained");

		exec_srv.scheduleAtFixedRate(renew, 110, 110, TimeUnit.MINUTES);
		return true;
	}

	public static synchronized AuthInfo getAgentAuthenticationInfo()
	{
		return agentAuthenticationInfo;
	}

	private static void renewCredentials()
	{
		AuthInfo newAgentAuthenticationInfo = null;
		try
		{
			newAgentAuthenticationInfo = authProvider.getCredentials();
			if (agentAuthenticationInfo == null)
			{
				log.warn("BrokerAuthenticationService - failed to renew credentials. Auth Info: " + agentAuthenticationInfo);
				return;
			}
		}
		catch (Exception e)
		{
			log.warn(String.format("BrokerAuthenticationService - failed to renew credentials. Auth ProviderInfo: %s ", authProvider), e);
			return;
		}

		synchronized (SapoSTSService.class)
		{
			agentAuthenticationInfo = newAgentAuthenticationInfo;
		}
		log.info("STS Credentials renewed");
	}

}
