package pt.com.broker.auth;


public interface AclPredicate
{
	boolean match(SessionProperties properties);
}
