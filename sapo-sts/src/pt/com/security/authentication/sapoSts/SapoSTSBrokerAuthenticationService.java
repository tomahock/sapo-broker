package pt.com.security.authentication.sapoSts;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.caudexorigo.concurrent.CustomExecutors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import pt.com.common.security.ClientAuthInfo;
import pt.com.common.security.authentication.AuthenticationCredentialsProvider;
import pt.com.common.security.authentication.AuthenticationCredentialsProviderFactory;
import pt.com.gcs.conf.GcsInfo;
import pt.com.gcs.conf.ProviderInfo;
import pt.com.security.authentication.sapoSts.SapoSTSAuthenticationParamsProvider.Parameters;

public class SapoSTSBrokerAuthenticationService
{
	private static final Logger log = LoggerFactory.getLogger(SapoSTSBrokerAuthenticationService.class);
	private static final ScheduledThreadPoolExecutor exec_srv = CustomExecutors.newScheduledThreadPool(1, "SapoSTS Authentication Sevice");

	private static ClientAuthInfo agentAuthenticationInfo;

	public static void start()
	{
		ProviderInfo providerInfo = GcsInfo.getAuthenticationProviders().get("SapoSTS");
		if( providerInfo == null) {
			log.error("Failed to obtain Authentication information from GcsInfo");
			return;
		}
		
		Element confNode = providerInfo.getParameters();
		if( confNode == null) {
			log.error("There is no configuration info regarding SapoSTS authentication provider.");
			return;
		}
		
		NodeList stsNode = confNode.getElementsByTagName("sts");
		if(stsNode.getLength() == 0)
		{
			log.error("Missing STS element in authentication information params");
			return;
		}
		
		Element stsElem = (Element) stsNode.item(0);
		
		String stsLocation = null;
		String stsUsername = null;
		String stsPassword = null;
		
		NodeList locationElems = stsElem.getElementsByTagName("STSLocation");
		if(locationElems.getLength() == 0)
		{
			log.error("Missing STSLocation info");
			return;
		}
		stsLocation = locationElems.item(0).getTextContent();
		
		NodeList usernameElems = stsElem.getElementsByTagName("STSUsername");
		if(usernameElems.getLength() == 0)
		{
			log.error("Missing STSUsername info");
			return;
		}
		stsUsername = usernameElems.item(0).getTextContent();
		
		NodeList passwordElems = stsElem.getElementsByTagName("STSPassword");
		if(passwordElems.getLength() == 0)
		{
			log.error("Missing STSPassword info");
			return;
		}
		stsPassword = passwordElems.item(0).getTextContent();

		Parameters parameters = new SapoSTSAuthenticationParamsProvider.Parameters(stsLocation, stsUsername, stsPassword);
		SapoSTSAuthenticationParamsProvider.setSTSParameters(parameters);

		ClientAuthInfo agentAuthInfo = new ClientAuthInfo(stsUsername, stsPassword);

		AuthenticationCredentialsProvider authProvider = AuthenticationCredentialsProviderFactory.getProvider("SapoSTS");
		try
		{
			agentAuthenticationInfo = authProvider.getCredentials(agentAuthInfo);
			if (agentAuthenticationInfo == null)
			{
				log.error("Failed to get credentials");
				return;
			}
		}
		catch (Exception e)
		{
			log.warn("Failed to get credentials for Service BUS. Reason: '{}'", e.getMessage());
			return;
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
	}

	public static ClientAuthInfo getAgentAuthenticationInfo()
	{
		synchronized (SapoSTSBrokerAuthenticationService.class)
		{
			return agentAuthenticationInfo;
		}
	}

	private static void renewCredentials()
	{
		ClientAuthInfo newAgentAuthenticationInfo = null;
		try
		{
			newAgentAuthenticationInfo = AuthenticationCredentialsProviderFactory.getProvider("SapoSTS").getCredentials(agentAuthenticationInfo);
			if (agentAuthenticationInfo == null)
			{
				log.warn("BrokerAuthenticationService - failed to renew credentials");
				return;
			}
		}
		catch (Exception e)
		{
			log.warn("BrokerAuthenticationService - failed to renew credentials", e);
			return;
		}

		synchronized (SapoSTSBrokerAuthenticationService.class)
		{
			agentAuthenticationInfo = newAgentAuthenticationInfo;
		}
		log.info("STS Credentials renewed");
	}

}
