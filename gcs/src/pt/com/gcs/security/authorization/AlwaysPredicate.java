package pt.com.broker.security.authorization;

import pt.com.broker.security.SessionProperties;

public class AlwaysPredicate implements AclPredicate
{

	private static AlwaysPredicate instance = new AlwaysPredicate();
	
	@Override
	public boolean match(SessionProperties properties)
	{
		// TODO Auto-generated method stub
		return true;
	}


	public static AlwaysPredicate getInstance()
	{
		return instance;
	}

	@Override
	public String toString()
	{
		return "AlwaysPredicate (Every request matches this predicate)";
	}
}
