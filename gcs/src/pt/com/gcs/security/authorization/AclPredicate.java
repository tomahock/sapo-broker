package pt.com.broker.security.authorization;

import pt.com.broker.security.SessionProperties;

public interface AclPredicate {
	boolean match(SessionProperties properties);
}
