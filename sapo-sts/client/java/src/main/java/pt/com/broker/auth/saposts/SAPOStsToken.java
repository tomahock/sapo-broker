package pt.com.broker.auth.saposts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import pt.com.broker.auth.InvalidCredentialsException;
import pt.com.broker.auth.saposts.SapoSTSParameterProvider.Parameters;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class SAPOStsToken
{
	private static final Logger log = LoggerFactory.getLogger(SAPOStsToken.class);

	public static final String DEFAULT_TTL = "2:00:00";
	public static final String DEFAULT_BASE_URL = "https://services.bk.sapo.pt/STS/";

	public static String getToken(String username, String password) throws Exception
	{
		Parameters stsParameters = SapoSTSParameterProvider.getSTSParameters();

		return getToken(username, password, ((stsParameters != null) && (stsParameters.getLocation() != null)) ? stsParameters.getLocation() : DEFAULT_BASE_URL);
	}

	public static String getToken(String username, String password, String baseUrl) throws Exception
	{
		return getToken(username, password, baseUrl, DEFAULT_TTL);
	}

	public static String getToken(String username, String password, String baseUrl, String tokenTTL) throws Exception
	{
		log.info("Getting STS token");

		String connectionUrl = getConnectionUrl(username, password, baseUrl, tokenTTL);

		URL url = new URL(connectionUrl);

		URLConnection connection = url.openConnection();

		HttpURLConnection httpUrlconn = (HttpURLConnection) connection;
		httpUrlconn.setConnectTimeout(500);
		httpUrlconn.setReadTimeout(4000);

		int respCode = httpUrlconn.getResponseCode();

		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = docBuilderFactory.newDocumentBuilder();
		InputStream inputStream = null;
		if (respCode == HttpURLConnection.HTTP_OK)
		{
			connection.getContent();
			inputStream = httpUrlconn.getInputStream();
		}
		else if (respCode == HttpURLConnection.HTTP_INTERNAL_ERROR)
		{
			inputStream = httpUrlconn.getErrorStream();
		}
		else
		{
			String errorMsg = "STS returned an unexpected code: " + respCode;
			throw new UnknownError(errorMsg);
		}

		Document doc = documentBuilder.parse(inputStream);
		Element documentElement = doc.getDocumentElement();

		String token = null;

		if (respCode == HttpURLConnection.HTTP_OK)
		{
			token = documentElement.getTextContent();
		}
		else
		{
			XPath xpath = XPathFactory.newInstance().newXPath();

			xpath.setNamespaceContext(pt.com.broker.auth.saposts.SapoSTSNamespaceContext.getInstance());

			Element codeElem = (Element) ((NodeList) xpath.evaluate("/fault/detail/exceptionInfo/code", doc, XPathConstants.NODESET)).item(0);
			if (codeElem.getTextContent().equals("1010"))
			{
				throw new InvalidCredentialsException();
			}
			else
			{
				String error = "STS fault with unexpected error code - " + codeElem.getTextContent();
				throw new UnknownError(error);
			}
		}
		return token;
	}

	private static String getConnectionUrl(String username, String password, String baseUrl, String tokenTTL)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(baseUrl);
		sb.append("GetToken?");

		sb.append("ESBUsername=");
		sb.append(username);
		sb.append("&");

		sb.append("ESBPassword=");
		sb.append(password);
		sb.append("&");

		sb.append("ESBTokenTimeToLive=");
		sb.append(tokenTTL);

		return sb.toString();
	}
}