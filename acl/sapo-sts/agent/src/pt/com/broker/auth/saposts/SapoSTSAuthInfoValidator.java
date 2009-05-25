package pt.com.broker.auth.saposts;

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

import pt.com.broker.auth.AuthInfo;
import pt.com.broker.auth.AuthInfoValidator;
import pt.com.broker.auth.AuthValidationResult;
import pt.com.broker.auth.ProviderInfo;
import pt.com.broker.auth.saposts.SapoSTSParameterProvider.Parameters;

public class SapoSTSAuthInfoValidator implements AuthInfoValidator
{

	public static class SapoSTSAuthValidationResult implements AuthValidationResult
	{
		private boolean valid;
		private String reasonForFailure;
		private List<String> roles;

		public SapoSTSAuthValidationResult(List<String> roles)
		{
			valid = true;
			this.roles = roles;
		}

		public SapoSTSAuthValidationResult(String reasonForFailure)
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

	private static final SapoSTSAuthValidationResult internalError = new SapoSTSAuthValidationResult("Internal error");

	@Override
	public AuthValidationResult validate(AuthInfo clientAuthInfo) throws Exception
	{
		URL url = new URL(getConnectionUrl(clientAuthInfo, SapoSTSService.getAgentAuthenticationInfo()));

		URLConnection connection = url.openConnection();

		if (!(connection instanceof HttpURLConnection))
		{
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
			return internalError;
		}

		Document doc = documentBuilder.parse(inputStream);

		XPath xpath = XPathFactory.newInstance().newXPath();
		xpath.setNamespaceContext(pt.com.broker.auth.saposts.SapoSTSNamespaceContext.getInstance());

		SapoSTSAuthValidationResult avr = null;

		if (respCode == HttpURLConnection.HTTP_OK)
		{
			NodeList roleNodes = ((NodeList) xpath.evaluate("/GetRolesResponse/GetRolesResult/ESBRoles/ESBRole", doc, XPathConstants.NODESET));

			List<String> roles = null;

			if (roleNodes.getLength() != 0)
			{
				roles = extractRoles(roleNodes);
			}
			else
			{
				roles = new ArrayList<String>(1);
			}

			avr = new SapoSTSAuthValidationResult(roles);
		}
		else
		{
			Element codeElem = (Element) ((NodeList) xpath.evaluate("/fault/detail/exceptionInfo/code", doc, XPathConstants.NODESET)).item(0);

			avr = new SapoSTSAuthValidationResult(SapoSTSCodeErrors.getErrorDescription(codeElem.getTextContent()));
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

	private String getConnectionUrl(AuthInfo clientAuthInfo, AuthInfo agentAuthInfo)
	{
		String sapoSTSUrl = null;

		Parameters parameters = SapoSTSParameterProvider.getSTSParameters();
		if (parameters != null && parameters.getLocation() != null)
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

	@Override
	public boolean init(ProviderInfo info)
	{
		return SapoSTSService.start(info);
	}	

}
