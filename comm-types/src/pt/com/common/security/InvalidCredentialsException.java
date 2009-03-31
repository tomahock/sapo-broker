package pt.com.common.security;

public class InvalidCredentialsException extends RuntimeException
{
	public InvalidCredentialsException()

	{
		super();
	}

	public InvalidCredentialsException(Throwable cause)
	{
		super(cause);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -2615267393952754028L;

}
