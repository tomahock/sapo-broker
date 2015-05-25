package pt.com.broker.auth.saposts;

import javax.xml.ws.soap.SOAPFaultException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.auth.InvalidCredentialsException;
import pt.sapo.services.definitions.ESBCredentials;
import pt.sapo.services.definitions.STSSoapSecure;

public class SAPOStsToken
{
	private static final Logger log = LoggerFactory.getLogger(SAPOStsToken.class);

	public static final String DEFAULT_TTL = "2:00:00";

	protected STSSoapSecure service;

	public SAPOStsToken(STSSoapSecure soapSecure)
	{
		this.service = soapSecure;
	}

	public String getToken(String username, String password) throws Exception
	{
		return getToken(username, password, DEFAULT_TTL);
	}

	public String getToken(ESBCredentials credentials) throws Exception
	{

		log.info("Getting STS token");

		try
		{

			String token = service.getToken(credentials, false);

			return token;

		}
		catch (SOAPFaultException fault)
		{

			if (fault.getFault().getFaultCode().equals("1010"))
			{
				throw new InvalidCredentialsException();
			}
			else
			{

				String error = "STS fault with unexpected error code - " + fault.getFault().getFaultCode();

				throw new UnknownError(error);
			}

		}
		catch (Throwable e)
		{

			log.error("Error", e);

			String errorMsg = "STS returned an unexpected code: " + e.getMessage();

			throw new UnknownError(errorMsg);

		}

	}

	public String getToken(String username, String password, String tokenTTL) throws Exception
	{

		ESBCredentials credentials = new ESBCredentials();

		credentials.setESBUsername(username);
		credentials.setESBPassword(password);
		credentials.setESBTokenTimeToLive(tokenTTL);

		return getToken(credentials);
	}

	/*
	 * private static String getConnectionUrl(String username, String password, String baseUrl, String tokenTTL) {
	 * 
	 * StringBuilder sb = new StringBuilder();
	 * 
	 * try { sb.append(baseUrl); sb.append("GetToken?");
	 * 
	 * sb.append("ESBUsername="); sb.append(URLEncoder.encode(username,"UTF-8"));
	 * 
	 * sb.append("&");
	 * 
	 * sb.append("ESBPassword=");
	 * 
	 * sb.append(URLEncoder.encode(password,"UTF-8"));
	 * 
	 * sb.append("&");
	 * 
	 * sb.append("ESBTokenTimeToLive="); sb.append(URLEncoder.encode(tokenTTL,"UTF-8"));
	 * 
	 * } catch (UnsupportedEncodingException e) { e.printStackTrace(); }
	 * 
	 * return sb.toString(); }
	 */
}