package pt.com.common.security;

public class InvalidTokenException extends RuntimeException
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6123036506161672260L;

	public InvalidTokenException()

	{
		super();
	}

	public InvalidTokenException(Throwable cause)
	{
		super(cause);
	}
	
}
