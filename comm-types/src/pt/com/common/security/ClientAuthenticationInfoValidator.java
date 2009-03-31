package pt.com.common.security;

public interface ClientAuthenticationInfoValidator
{
	ClientAuthenticationInfoValidationResult validate(ClientAuthInfo clientAuthInfo, ClientAuthInfo agentAuthInfo) throws Exception;
}
