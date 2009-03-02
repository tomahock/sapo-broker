package pt.com.gcs.security;

public interface AclPredicate {
	boolean match(SessionProperties properties);
}
