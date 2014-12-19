package pt.com.broker.functests.helpers;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SetValueFuture<T> implements Future<T>
{
	
	private static final Logger log = LoggerFactory.getLogger(SetValueFuture.class);
	
	private T value = null;
	private Object syncObj = new Object();

	@Override
	public boolean cancel(boolean mayInterruptIfRunning)
	{
		return false;
	}

	@Override
	public T get() throws InterruptedException, ExecutionException
	{
		log.debug("SetValueFuture get method called.");
		synchronized (syncObj)
		{
			log.debug("SetValueFuture get method called - passed synchronized.");
			if (value == null)
			{
				log.debug("Whaiting for value to became non null.");
				syncObj.wait();
			}
			log.debug("Returning value.");
			return value;
		}
	}

	@Override
	public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException
	{
		log.debug("SetValueFuture get method called.");
		synchronized (syncObj)
		{
			log.debug("SetValueFuture get method called - passed synchronized.");
			if (value == null)
			{
				log.debug("Whaiting for value to became non null. Timeout set for {} miliseconds.", timeout);
				syncObj.wait(unit.toMillis(timeout));
				// TODO: test for timeout. Given object usage it's acceptable not to test...
			}
			return value;
		}
	}

	@Override
	public boolean isCancelled()
	{
		return false;
	}

	@Override
	public boolean isDone()
	{
		log.debug("SetValueFuture isDone method called.");
		synchronized (syncObj)
		{
			log.debug("SetValueFuture isDone method called - passed synchronized");
			return value != null;
		}
	}

	public void set(T value)
	{
		log.debug("SetValueFuture isDone method called.");
		synchronized (syncObj)
		{
			log.debug("SetValueFuture set method called - passed synchronized");
			this.value = value;
			syncObj.notifyAll();
		}
	}

}
