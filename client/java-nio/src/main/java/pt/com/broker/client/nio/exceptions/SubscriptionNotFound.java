package pt.com.broker.client.nio.exceptions;

/**
 * Copyright (c) 2014, SAPO All rights reserved.
 *
 * <p/>
 * Created by Luis Santos<luis.santos@telecom.pt> on 14-07-2014.
 */
public class SubscriptionNotFound extends Exception
{

	public SubscriptionNotFound()
	{
		super();
	}

	public SubscriptionNotFound(String message)
	{
		super(message);
	}

	public SubscriptionNotFound(String message, Throwable cause)
	{
		super(message, cause);
	}

	public SubscriptionNotFound(Throwable cause)
	{
		super(cause);
	}

	public SubscriptionNotFound(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
