package pt.com.broker.auth;

import java.util.List;

/**
 * AndPredicate represents a set of predicates.
 * 
 */

public class AndPredicate implements AclPredicate
{

	private List<AclPredicate> predicates;

	public AndPredicate(List<AclPredicate> predicates)
	{
		this.predicates = predicates;
	}

	@Override
	public boolean match(SessionProperties properties)
	{
		for (AclPredicate pred : predicates)
		{
			if (!pred.match(properties))
				return false;
		}
		return true;
	}

	public List<AclPredicate> getPredicates()
	{
		return predicates;
	}

	@Override
	public String toString()
	{
		return "AndPredicate";
	}

}
