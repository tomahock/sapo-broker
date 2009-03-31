package pt.com.common.security;

public class TokenExpiredException extends RuntimeException
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5929750700874708487L;

	public TokenExpiredException()

	{
		super();
	}

	public TokenExpiredException(Throwable cause)
	{
		super(cause);
	}
}
