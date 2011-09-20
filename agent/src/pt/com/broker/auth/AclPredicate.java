package pt.com.broker.auth;

/**
 * AclPredicate interface should be implemented by objects representing security policiy predicates.
 * 
 */

public interface AclPredicate
{
	boolean match(SessionProperties properties);
}
