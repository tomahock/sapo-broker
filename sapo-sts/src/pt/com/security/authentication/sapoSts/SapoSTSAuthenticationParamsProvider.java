package pt.com.security.authentication.sapoSts;

public class SapoSTSAuthenticationParamsProvider
{
	public static class Parameters
	{
		private String location;
		private String username;
		private String password;

		public Parameters(String location, String username, String password)
		{
			this.location = location;
			this.username = username;
			this.password = password;
		}
		
		public Parameters(String location)
		{
			this(location, null, null);
		}

		public String getLocation()
		{
			return location;
		}

		public String getUsername()
		{
			return username;
		}

		public String getPassword()
		{
			return password;
		}
	}

	private static Parameters params = null;

	public static void setSTSParameters(Parameters parameters)
	{
		params = parameters;
	}

	public static Parameters getSTSParameters()
	{
		return params;
	}

}
