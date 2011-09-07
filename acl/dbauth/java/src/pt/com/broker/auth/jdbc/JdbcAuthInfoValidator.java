package pt.com.broker.auth.jdbc;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import pt.com.broker.auth.AuthInfo;
import pt.com.broker.auth.AuthInfoValidator;
import pt.com.broker.auth.AuthValidationResult;
import pt.com.broker.auth.ProviderInfo;

/**
 * JdbcAuthInfoValidator implements AuthInfoValidator in order to provide authentication based in database credentials and roles. This was implemented as a proof-of-concept.
 * 
 */
public class JdbcAuthInfoValidator implements AuthInfoValidator
{

	private static final Logger log = LoggerFactory.getLogger(JdbcAuthInfoValidator.class);

	public static class JdbcAuthValidationResult implements AuthValidationResult
	{
		private boolean valid;
		private String reasonForFailure;
		private List<String> roles;

		public JdbcAuthValidationResult(List<String> roles)
		{
			valid = true;
			this.roles = roles;
		}

		public JdbcAuthValidationResult(String reasonForFailure)
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

	private static final JdbcAuthValidationResult invalidToken = new JdbcAuthValidationResult("Invalid Token");
	private static final JdbcAuthValidationResult internalError = new JdbcAuthValidationResult("Internal error");

	JdbcRoles jdbcRoles = null;

	@Override
	public boolean init(ProviderInfo info)
	{
		Element parameters = info.getParameters();
		NodeList dbRolesNode = parameters.getElementsByTagName("db-roles");
		if (dbRolesNode.getLength() == 0)
		{
			log.error("Missing db-roles element in authentication information params");
			return false;
		}

		Element dbRolesElem = (Element) dbRolesNode.item(0);

		String driverClass;
		String databaseUrl;
		String databaseUsername;
		String databasePassword;

		NodeList driverClassElems = dbRolesElem.getElementsByTagName("driver-class");
		if (driverClassElems.getLength() == 0)
		{
			log.error("Missing driver-class info");
			return false;
		}
		driverClass = driverClassElems.item(0).getTextContent();

		NodeList databaseUrlElems = dbRolesElem.getElementsByTagName("database-url");
		if (databaseUrlElems.getLength() == 0)
		{
			log.error("Missing database-url info");
			return false;
		}
		databaseUrl = databaseUrlElems.item(0).getTextContent();

		NodeList databaseUsernameElems = dbRolesElem.getElementsByTagName("database-username");
		if (databaseUsernameElems.getLength() == 0)
		{
			log.error("Missing database-username info");
			return false;
		}
		databaseUsername = databaseUsernameElems.item(0).getTextContent();

		NodeList databasePasswordElems = dbRolesElem.getElementsByTagName("database-password");
		if (databasePasswordElems.getLength() == 0)
		{
			log.error("Missing database-password info");
			return false;
		}
		databasePassword = databasePasswordElems.item(0).getTextContent();

		jdbcRoles = new JdbcRoles(driverClass, databaseUrl, databaseUsername, databasePassword);
		if (!jdbcRoles.init())
		{
			log.error("Failed to initialize JdbcRoles");
			return false;
		}

		return true;
	}

	@Override
	public AuthValidationResult validate(AuthInfo clientAuthInfo) throws Exception
	{
		JdbcAuthValidationResult res = null;
		try
		{
			if (jdbcRoles.validate(clientAuthInfo.getUserId(), new String(clientAuthInfo.getToken())))
			{
				List<String> roles = jdbcRoles.getRoles(clientAuthInfo.getUserId());
				res = new JdbcAuthValidationResult(roles);
			}
			else
			{
				return invalidToken;
			}
		}
		catch (Throwable t)
		{
			log.error("Failed to authenticate user.", t);
			return internalError;
		}

		return res;
	}
}
