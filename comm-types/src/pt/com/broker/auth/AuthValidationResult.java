package pt.com.broker.auth;

import java.util.List;

public interface AuthValidationResult
{
	boolean areCredentialsValid();

	String getReasonForFailure();

	List<String> getRoles();
}
