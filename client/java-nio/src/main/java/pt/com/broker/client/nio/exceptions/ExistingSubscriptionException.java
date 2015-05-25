package pt.com.broker.client.nio.exceptions;

public class ExistingSubscriptionException extends RuntimeException
{

	private static final long serialVersionUID = 1L;

	public ExistingSubscriptionException(String message)
	{
		super(message);
	}

}
