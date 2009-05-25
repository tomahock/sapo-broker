package pt.com.broker.auth.saposts;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import pt.com.broker.auth.AuthInfo;
import pt.com.broker.auth.CredentialsProvider;
import pt.com.broker.auth.InvalidCredentialsException;
import pt.com.broker.auth.ProviderInfo;
import pt.com.broker.auth.saposts.SapoSTSParameterProvider.Parameters;

public class SapoSTSProvider implements CredentialsProvider
{

	private static final Logger log = LoggerFactory.getLogger(SapoSTSProvider.class);

	private static final String tokenTTL = "2:00:00";
	
	private String providerName = null;

	@Override
	public AuthInfo getCredentials(AuthInfo clientAuthInfo) throws Exception
	{
		URL url = new URL(getConnectionUrl(clientAuthInfo));

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
			log.error("STS returned an unexpected code: " + respCode);
			return null;
		}

		Document doc = documentBuilder.parse(inputStream);
		Element documentElement = doc.getDocumentElement();

		if (respCode == HttpURLConnection.HTTP_OK)
		{
			byte[] token = null;

			token = documentElement.getTextContent().getBytes(Charset.forName("UTF-8"));

			AuthInfo aui = new AuthInfo(clientAuthInfo.getUserId(), clientAuthInfo.getRoles(), token, clientAuthInfo.getUserAuthenticationType());
			return aui;
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
				log.error(error);
				throw new UnknownError(error);
			}
		}
	}

	private String getConnectionUrl(AuthInfo clientAuthInfo)
	{
		String sapoSTSgetTokenUrl = null;

		Parameters parameters = SapoSTSParameterProvider.getSTSParameters();
		if (parameters != null && parameters.getLocation() != null)
			sapoSTSgetTokenUrl = parameters.getLocation();

		StringBuilder sb = new StringBuilder();
		sb.append(sapoSTSgetTokenUrl);
		sb.append("GetToken?");

		sb.append("ESBUsername=");
		sb.append(clientAuthInfo.getUserId());
		sb.append("&");

		sb.append("ESBPassword=");
		sb.append(new String(clientAuthInfo.getToken()));
		sb.append("&");

		sb.append("ESBTokenTimeToLive=");
		sb.append(tokenTTL);

		return sb.toString();
	}

	@Override
	public boolean init(ProviderInfo info)
	{
		providerName = info.getName();	
		return true;
	}

	@Override
	public String getAuthenticationType()
	{
		return providerName;
	}

}
