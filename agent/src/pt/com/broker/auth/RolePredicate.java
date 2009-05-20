package pt.com.broker.auth;

import java.util.List;


public class RolePredicate implements AclPredicate
{

	private String role;

	public RolePredicate(String role)
	{
		this.role = role;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean match(SessionProperties properties)
	{
		List<String> roles = properties.getRoles();
		if (roles == null)
		{
			return false;
		}
		for (String r : roles)
		{
			if (role.equals(r))
				return true;
		}

		return false;
	}

	public String getRole()
	{
		return role;
	}

	@Override
	public String toString()
	{
		return "RolePredicate (" + role + ")";
	}

}
