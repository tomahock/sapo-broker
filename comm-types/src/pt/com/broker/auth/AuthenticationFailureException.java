package pt.com.broker.auth;

//TODO: make this checked!!
public class AuthenticationFailureException extends RuntimeException
{

	private static final long serialVersionUID = 444849596643329916L;

	public AuthenticationFailureException(String reason)
	{
		this(reason, null);
	}

	public AuthenticationFailureException(String reason, Throwable cause)
	{
		super(reason, cause);
	}
}
