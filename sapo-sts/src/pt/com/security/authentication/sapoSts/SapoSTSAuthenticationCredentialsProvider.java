package pt.com.security.authentication.sapoSts;

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

import pt.com.common.security.ClientAuthInfo;
import pt.com.common.security.InvalidCredentialsException;
import pt.com.common.security.authentication.AuthenticationCredentialsProvider;
import pt.com.security.authentication.sapoSts.SapoSTSAuthenticationParamsProvider.Parameters;

public class SapoSTSAuthenticationCredentialsProvider implements AuthenticationCredentialsProvider
{

	private static final Logger log = LoggerFactory.getLogger(SapoSTSAuthenticationCredentialsProvider.class);
	
	private static final String tokenTTL = "2:00:00";

	@Override
	public ClientAuthInfo getCredentials(ClientAuthInfo clientAuthInfo) throws Exception
	{
		URL url = new URL(getConnectionUrl(clientAuthInfo));

		URLConnection connection = url.openConnection();

		if (!(connection instanceof HttpURLConnection))
		{
			return null;
		}

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
			return null;
		}

		Document doc = documentBuilder.parse(inputStream);
		Element documentElement = doc.getDocumentElement();

		if (respCode == HttpURLConnection.HTTP_OK)
		{
			byte[] token = null;

			token = documentElement.getTextContent().getBytes(Charset.forName("UTF-8"));

			ClientAuthInfo aui = new ClientAuthInfo(clientAuthInfo.getUserId(), clientAuthInfo.getRoles(), token, clientAuthInfo.getUserAuthenticationType(), clientAuthInfo.getPassword());
			return aui;
		}
		else
		{

			XPath xpath = XPathFactory.newInstance().newXPath();
			
			xpath.setNamespaceContext(pt.com.common.security.SapoSTSNamespaceContext.getInstance());

			Element codeElem = (Element) ((NodeList) xpath.evaluate("/fault/detail/exceptionInfo/code", doc, XPathConstants.NODESET)).item(0);
			if (codeElem.getTextContent().equals("1010"))
			{
				throw new InvalidCredentialsException();
			}
			else
			{
				// TODO: decide what to throw
			}
		}

		return null;
	}
	
	private String getConnectionUrl(ClientAuthInfo clientAuthInfo)
	{
		String sapoSTSgetTokenUrl = "https://services.sapo.pt/STS/";
		//String sapoSTSgetTokenUrl = "http://ESB/STS/";
		
		Parameters parameters = SapoSTSAuthenticationParamsProvider.getSTSParameters();
		if( parameters != null && parameters.getLocation() != null )
			sapoSTSgetTokenUrl = parameters.getLocation();
		
		StringBuilder sb = new StringBuilder();
		sb.append(sapoSTSgetTokenUrl);
		sb.append("GetToken?");

		sb.append("ESBUsername=");
		sb.append(clientAuthInfo.getUserId());
		sb.append("&");

		sb.append("ESBPassword=");
		sb.append(clientAuthInfo.getPassword());
		sb.append("&");

		sb.append("ESBTokenTimeToLive=");
		sb.append(tokenTTL);

		return sb.toString();
	}

	@Override
	public void init()
	{
		// TODO Auto-generated method stub
		
	}

}
