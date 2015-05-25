package pt.com.broker.auth.saposts;

import java.net.URL;

import javax.xml.ws.BindingProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import pt.com.broker.auth.AuthInfo;
import pt.com.broker.auth.CredentialsProvider;
import pt.com.broker.auth.ProviderInfo;
import pt.sapo.services.definitions.ESBCredentials;
import pt.sapo.services.definitions.STS;
import pt.sapo.services.definitions.STSSoapSecure;
import pt.sapo.services.definitions.UserInfo;

/**
 * SapoSTSService is responsible for managing agent's credentials (STS Token) to Sapo STS and automatically renew it.
 * 
 */

public class SapoSTSService
{
	private static final Logger log = LoggerFactory.getLogger(SapoSTSService.class);

	private static AuthInfo agentAuthenticationInfo = null;
	private static CredentialsProvider authProvider = null;

	protected STSSoapSecure soapSecure;

	private static ESBCredentials credentials;

	public static ESBCredentials getCredentials()
	{
		return credentials;
	}

	protected Node getConfig(Element element, String name)
	{

		NodeList node = element.getElementsByTagName(name);

		if (node.getLength() == 0)
		{
			log.error("Missing '{}' element in validation provider params", name);
			throw new RuntimeException(String.format("Node %s not found", name));
		}

		return node.item(0);

	}

	public boolean start(ProviderInfo providerInfo)
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

		String stsPassword = null;
		String stsUsername = null;
		String stsLocation = null;
		String stsToken = null;

		try
		{

			Element stsElem = (Element) getConfig(confNode, "sts");

			stsLocation = getConfig(stsElem, "sts-location").getTextContent();

			if ((stsElem.getElementsByTagName("sts-username").getLength() > 0 ||
					stsElem.getElementsByTagName("sts-password").getLength() > 0) &&
					stsElem.getElementsByTagName("sts-token").getLength() > 0)
			{

				throw new RuntimeException("You can have either Token or Username Password, not both");

			}

			if (stsElem.getElementsByTagName("sts-username").getLength() > 0 ||
					stsElem.getElementsByTagName("sts-password").getLength() > 0)
			{

				log.warn("You should not use raw credentials. Please use an ESBToken.");
				stsUsername = getConfig(stsElem, "sts-username").getTextContent();
				stsPassword = getConfig(stsElem, "sts-password").getTextContent();

			}
			else
			{

				stsToken = getConfig(stsElem, "sts-token").getTextContent();

			}

		}
		catch (Throwable t)
		{

			log.error("Config error", t);

			return false;
		}

		credentials = new ESBCredentials();

		credentials.setESBUsername(stsUsername);
		credentials.setESBPassword(stsPassword);
		credentials.setESBToken(stsToken);

		try
		{

			soapSecure = getClient(stsLocation);

			soapSecure.getPrimaryId(credentials, null, false, null, null);

		}
		catch (Throwable e)
		{
			log.warn("Failed to get credentials for Service BUS.", e);
			return false;
		}

		log.info("STS Credentials obtained");

		return true;
	}

	public UserInfo getUserInfo(String token)
	{

		ESBCredentials credentials = new ESBCredentials();

		credentials.setESBToken(token);

		UserInfo info = soapSecure.getPrimaryId(credentials, null, false, null, null);

		return info;
	}

	protected UserInfo getPrimaryIdDetails(String user_email)
	{
		return getPrimaryIdDetails(user_email, "sapo");
	}

	protected UserInfo getPrimaryIdDetails(String user_email, String credentialstore)
	{

		ESBCredentials credentials = getCredentials();

		return soapSecure.getPrimaryIdDetails(credentials, user_email, false, null, credentialstore);

	}

	public STSSoapSecure getClient(String base_url)
	{

		URL url = null;

		try
		{

			url = STSSoapSecure.class.getClassLoader().getResource("STS.wsdl");

			STS sts = new STS(url);

			STSSoapSecure secure = sts.getSTSSoapSecure();

			BindingProvider bp = (BindingProvider) secure;

			bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, base_url);

			return secure;

		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}

		return null;

	}

}
