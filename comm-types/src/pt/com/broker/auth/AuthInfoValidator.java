package pt.com.broker.auth;

/**
 * AuthInfoValidator should be implemented by authentication providers who validate client credentials and translate them into client roles.
 * 
 */

public interface AuthInfoValidator
{
	/**
	 * Client's authentication information validation.
	 * 
	 * @param clientAuthInfo
	 *            Client's authentication information.
	 * @return An implementation of AuthValidationResult
	 * @throws Exception
	 *             Thrown when some error occurs during validation.
	 */
	AuthValidationResult validate(AuthInfo clientAuthInfo) throws Exception;

	/**
	 * Provider's initialization.
	 * 
	 * @param info
	 *            A Provider Info instance.
	 * @return <code>true</code> if the initialization was successful <code>false</code> otherwise.
	 */
	boolean init(ProviderInfo info);
}
