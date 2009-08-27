package pt.com.broker.auth;

import java.util.List;

/*
 * AuthValidationResult interface represents a client's credential validation result and should be implemented by different credential validator modules.
 */

public interface AuthValidationResult
{
	/**
	 * Determines if the credentials validation was successful.
	 * 
	 * @return <code>true</code> if successful <code>false</code> otherwise.
	 */
	boolean areCredentialsValid();

	/**
	 * This should only be consulted if the credentials are invalid.
	 * 
	 * @return A String describing the error.
	 */
	String getReasonForFailure();

	/**
	 * This should only be consulted if the credentials are valid.
	 * 
	 * @return A list of roles associated with the client.
	 */
	List<String> getRoles();
}
