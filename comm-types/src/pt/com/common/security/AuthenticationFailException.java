package pt.com.common.security;


//TODO: make this checked!!
public class AuthenticationFailException extends RuntimeException
{
	
	public AuthenticationFailException(String reason)
	{
		this(reason, null);
	}
	public AuthenticationFailException(String reason, Throwable cause)
	{
		super(reason, cause);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 444849596643329916L;

}
