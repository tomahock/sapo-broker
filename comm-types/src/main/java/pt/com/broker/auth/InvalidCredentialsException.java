package pt.com.broker.auth;

public class InvalidCredentialsException extends RuntimeException
{
	private static final long serialVersionUID = -2615267393952754028L;

	public InvalidCredentialsException()
	{
		super();
	}

	public InvalidCredentialsException(Throwable cause)
	{
		super(cause);
	}

}
