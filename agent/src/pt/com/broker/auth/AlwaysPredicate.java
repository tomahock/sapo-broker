package pt.com.broker.auth;

/**
 * AlwaysPredicate is always <code>true</code>.
 * 
 */

public class AlwaysPredicate implements AclPredicate
{

	private static AlwaysPredicate instance = new AlwaysPredicate();

	@Override
	public boolean match(SessionProperties properties)
	{
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
