package pt.com.gcs.security;

import java.util.List;

public class RolePredicate implements AclPredicate {

	private String role;
	
	public RolePredicate(String role)
	{
		this.role = role;
	}	
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean match(SessionProperties properties) {
		
		Object _roles = properties.get("ROLES");
		if(_roles == null)
		{
			return false;
		}
		List<String> roles = (List<String>) _roles;
		for(String r : roles)
		{
			if(role.equals(r))
				return true;
		}
		
		
		return false;
	}

	public String getRole() {
		return role;
	}
	
	@Override
	public String toString() {
		return "RolePredicate (" + role + ")";
	}

}
