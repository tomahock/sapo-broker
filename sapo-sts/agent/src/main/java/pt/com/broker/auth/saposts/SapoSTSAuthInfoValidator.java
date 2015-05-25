package pt.com.broker.auth.saposts;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.xml.ws.soap.SOAPFaultException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.auth.AuthInfo;
import pt.com.broker.auth.AuthInfoValidator;
import pt.com.broker.auth.AuthValidationResult;
import pt.com.broker.auth.ProviderInfo;
import pt.sapo.services.definitions.UserInfo;

/**
 * SapoSTSAuthInfoValidator implements AuthInfoValidator in order to provide Sapo STS based authentication, that is, given a Sapo STS token it extracts the associated user roles.
 * 
 */

public class SapoSTSAuthInfoValidator implements AuthInfoValidator
{

	private static final Logger log = LoggerFactory.getLogger(SapoSTSAuthInfoValidator.class);

	public static class SapoSTSAuthValidationResult implements AuthValidationResult
	{
		private boolean valid;
		private String reasonForFailure;
		private List<String> roles;

		public SapoSTSAuthValidationResult(List<String> roles)
		{
			valid = true;
			this.roles = roles;
		}

		public SapoSTSAuthValidationResult(String reasonForFailure)
		{
			valid = false;
			this.reasonForFailure = reasonForFailure;
		}

		@Override
		public boolean areCredentialsValid()
		{
			return valid;
		}

		@Override
		public String getReasonForFailure()
		{
			return reasonForFailure;
		}

		@Override
		public List<String> getRoles()
		{
			return roles;
		}
	}

	private static final SapoSTSAuthValidationResult internalError = new SapoSTSAuthValidationResult("Internal error");

	private SapoSTSService service;

	protected List<String> extractRoles(UserInfo userInfo)
	{

		if (userInfo.getESBRoles() != null && userInfo.getESBRoles().getESBRole() != null)
		{
			return userInfo.getESBRoles().getESBRole();
		}

		return new ArrayList<String>(1);
	}

	@Override
	public AuthValidationResult validate(AuthInfo clientAuthInfo) throws Exception
	{

		SapoSTSAuthValidationResult avr;

		try
		{

			UserInfo userInfo = authenticate(new String(clientAuthInfo.getToken(), "UTF-8"), clientAuthInfo.getUserId());

			avr = new SapoSTSAuthValidationResult(extractRoles(userInfo));

		}
		catch (SOAPFaultException fault)
		{

			fault.printStackTrace();

			avr = new SapoSTSAuthValidationResult(SapoSTSCodeErrors.getErrorDescription(fault.getMessage()));

		}
		catch (Throwable e)
		{

			e.printStackTrace();

			log.debug("Auth Error: {}", e.getMessage(), e);

			return internalError;
		}

		return avr;

	}

	@Override
	public boolean init(ProviderInfo info)
	{
		service = new SapoSTSService();

		return service.start(info);
	}

	protected boolean safeCompare(String stringA, String stringB)
	{

		byte[] a = stringA.getBytes(Charset.forName("UTF-8"));
		byte[] b = stringB.getBytes(Charset.forName("UTF-8"));

		if (a.length != b.length)
		{
			return false;
		}

		int result = 0;
		for (int i = 0; i < a.length; i++)
		{
			result |= a[i] ^ b[i];
		}

		return result == 0;

	}

	protected UserInfo authenticate(String token, String usernameOrEmail) throws Exception
	{

		UserInfo userInfoAuth = service.getUserInfo(token);

		UserInfo userInfoDetails = service.getPrimaryIdDetails(usernameOrEmail);

		if (!safeCompare(userInfoAuth.getPrimaryId(), userInfoDetails.getPrimaryId()))
		{
			throw new Exception("Invalid credenctials");
		}

		return userInfoDetails;
	}

}
