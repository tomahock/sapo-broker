package pt.com.common.security;

public interface ClientAuthenticationInfoValidator
{
	ClientAuthenticationInfoValidationResult validate(ClientAuthInfo clientAuthInfo) throws Exception;
	void init();
}
