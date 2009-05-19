package pt.com.security.authentication.databaseAccessControl;

import java.util.List;

import pt.com.common.security.ClientAuthInfo;
import pt.com.common.security.ClientAuthenticationInfoValidationResult;
import pt.com.common.security.ClientAuthenticationInfoValidator;

public class DBAuthenticationInfoValidator implements ClientAuthenticationInfoValidator
{

	public static class DBAccessControlClientAuthenticationInfoValidationResult implements ClientAuthenticationInfoValidationResult
	{
		private boolean valid;
		private String reasonForFailure;
		private List<String> roles;

		public DBAccessControlClientAuthenticationInfoValidationResult(List<String> roles)
		{
			valid = true;
			this.roles = roles;
		}

		public DBAccessControlClientAuthenticationInfoValidationResult(String reasonForFailure)
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

	private static final DBAccessControlClientAuthenticationInfoValidationResult invalidToken = new DBAccessControlClientAuthenticationInfoValidationResult("Invalid Token");
	private static final DBAccessControlClientAuthenticationInfoValidationResult internalError = new DBAccessControlClientAuthenticationInfoValidationResult("Internal error");

	@Override
	public void init()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public ClientAuthenticationInfoValidationResult validate(ClientAuthInfo clientAuthInfo) throws Exception
	{
		DBAccessControlClientAuthenticationInfoValidationResult res = null;
		try
		{
			if (PostgreSQLBrokerRoles.validate(clientAuthInfo.getUserId(), new String(clientAuthInfo.getToken())))
			{
				List<String> roles = PostgreSQLBrokerRoles.getRoles(clientAuthInfo.getUserId());
				res = new DBAccessControlClientAuthenticationInfoValidationResult(roles);
			}
			else
			{
				return invalidToken;
			}
		}
		catch (Throwable t)
		{
			return internalError;
		}

		return res;
	}

}
