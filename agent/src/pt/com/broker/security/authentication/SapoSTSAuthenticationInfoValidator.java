package pt.com.broker.security.authentication;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import pt.com.common.security.ClientAuthInfo;
import pt.com.common.security.ClientAuthenticationInfoValidationResult;
import pt.com.common.security.ClientAuthenticationInfoValidator;
import pt.com.common.security.InvalidCredentialsException;
import pt.com.common.security.InvalidTokenException;
import pt.com.common.security.SapoSTSCodeErrors;
import pt.com.common.security.TokenExpiredException;
import pt.com.common.security.authentication.SapoSTSAuthenticationParamsProvider;
import pt.com.common.security.authentication.SapoSTSAuthenticationParamsProvider.Parameters;

public class SapoSTSAuthenticationInfoValidator implements ClientAuthenticationInfoValidator
{

	public static class SapoSTEClientAuthenticationInfoValidationResult implements ClientAuthenticationInfoValidationResult
	{
		private boolean valid;
		private String reasonForFailure;
		private List<String> roles;

		public SapoSTEClientAuthenticationInfoValidationResult(List<String> roles)
		{
			valid = true;
			this.roles = roles;
		}

		public SapoSTEClientAuthenticationInfoValidationResult(String reasonForFailure)
		{
			valid = false;
			this.reasonForFailure = reasonForFailure;
		}

		@Override
		public boolean areCredentialsValid()
		{
			return valid;
		}

		@Override
		public String getReasonForFailure()
		{
			return reasonForFailure;
		}

		@Override
		public List<String> getRoles()
		{
			return roles;
		}

	}

	private static SapoSTEClientAuthenticationInfoValidationResult invalidToken = new SapoSTEClientAuthenticationInfoValidationResult("Invalid Token");
	private static SapoSTEClientAuthenticationInfoValidationResult tokenExpired = new SapoSTEClientAuthenticationInfoValidationResult("Token Expired");
	private static SapoSTEClientAuthenticationInfoValidationResult internalError = new SapoSTEClientAuthenticationInfoValidationResult("Internal error");

	@Override
	public ClientAuthenticationInfoValidationResult validate(ClientAuthInfo clientAuthInfo, ClientAuthInfo agentAuthInfo) throws Exception
	{
		URL url = new URL(getConnectionUrl(clientAuthInfo, agentAuthInfo));

		URLConnection connection = url.openConnection();

		if (!(connection instanceof HttpURLConnection))
		{
			// TODO: Solve this... Something strange happened! Throw Exception...
			return internalError;
		}

		HttpURLConnection httpUrlconn = (HttpURLConnection) connection;
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
			// TODO: Solve this... Something strange happened! Throw Exception...
			// BAD; VERY BAD...
			return internalError;
		}

		Document doc = documentBuilder.parse(inputStream);
		Element documentElement = doc.getDocumentElement();

		XPath xpath = XPathFactory.newInstance().newXPath();
		xpath.setNamespaceContext(pt.com.common.security.SapoSTSNamespaceContext.getInstance());

		SapoSTEClientAuthenticationInfoValidationResult avr = null;
		
		if (respCode == HttpURLConnection.HTTP_OK)
		{
			NodeList roleNodes = ((NodeList) xpath.evaluate("/GetRolesResponse/GetRolesResult/ESBRoles/ESBRole", doc, XPathConstants.NODESET));
			
			//NodeList roleNodes = ((NodeList) xpath.evaluate("//Roles", doc, XPathConstants.NODESET));

			List<String> roles = null;

			if (roleNodes.getLength() != 0)
			{
				roles = extractRoles(roleNodes);
			}
			else
			{
				roles = new ArrayList<String>(1);
			}

			avr = new SapoSTEClientAuthenticationInfoValidationResult(roles);
		}
		else
		{
			Element codeElem = (Element) ((NodeList) xpath.evaluate("/fault/detail/exceptionInfo/code", doc, XPathConstants.NODESET)).item(0);

			avr = new SapoSTEClientAuthenticationInfoValidationResult(SapoSTSCodeErrors.getErrorDescription(codeElem.getTextContent()));
		}

		return avr;
	}

	private List<String> extractRoles(NodeList roleNodes)
	{
		List<String> roles = new ArrayList<String>(roleNodes.getLength());

		for (int i = 0; i != roleNodes.getLength(); ++i)
		{
			Node item = roleNodes.item(i);
			if (item instanceof Element)
			{
				Element elem = (Element) item;
				roles.add(elem.getTextContent());
			}
		}

		return roles;
	}

	private String getConnectionUrl(ClientAuthInfo clientAuthInfo, ClientAuthInfo agentAuthInfo)
	{
		String sapoSTSUrl = "https://services.sapo.pt/STS/";
		
		
		Parameters parameters = SapoSTSAuthenticationParamsProvider.getSTSParameters();
		if( parameters != null && parameters.getLocation() != null )
			sapoSTSUrl = parameters.getLocation();
		
		StringBuilder sb = new StringBuilder();
		sb.append(sapoSTSUrl);
		sb.append("GetRoles?");

		sb.append("ESBToken=");
		sb.append(new String(agentAuthInfo.getToken(), Charset.forName("UTF-8")));

		sb.append("&UserToken=");
		sb.append(new String(clientAuthInfo.getToken(), Charset.forName("UTF-8")));

		return sb.toString();
	}

}
