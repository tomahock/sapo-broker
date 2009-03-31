package pt.com.common.security;

import java.util.List;

public interface ClientAuthenticationInfoValidationResult {
	boolean areCredentialsValid();
	String getReasonForFailure();
	List<String>  getRoles();
}
