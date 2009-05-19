package pt.com.broker.auth.jdbc;

import java.util.List;

import pt.com.broker.auth.AuthInfo;
import pt.com.broker.auth.AuthValidationResult;
import pt.com.broker.auth.AuthInfoValidator;

public class JdbcAuthInfoValidator implements AuthInfoValidator
{

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

	@Override
	public void init()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public AuthValidationResult validate(AuthInfo clientAuthInfo) throws Exception
	{
		JdbcAuthValidationResult res = null;
		try
		{
			if (JdbcRoles.validate(clientAuthInfo.getUserId(), new String(clientAuthInfo.getToken())))
			{
				List<String> roles = JdbcRoles.getRoles(clientAuthInfo.getUserId());
				res = new JdbcAuthValidationResult(roles);
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
