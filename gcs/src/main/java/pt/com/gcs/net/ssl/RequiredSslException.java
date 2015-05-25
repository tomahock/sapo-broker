package pt.com.gcs.net.ssl;

public class RequiredSslException extends RuntimeException
{

	private static final long serialVersionUID = 1L;

	public RequiredSslException()
	{
		super();
	}

	public RequiredSslException(String message)
	{
		super(message);
	}

}
