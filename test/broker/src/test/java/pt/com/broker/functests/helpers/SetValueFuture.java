package pt.com.broker.functests.helpers;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SetValueFuture<T> implements Future<T>
{
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
		synchronized (syncObj)
		{
			if (value == null)
			{
				syncObj.wait();
			}
			return value;
		}
	}

	@Override
	public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException
	{
		synchronized (syncObj)
		{
			if (value == null)
			{
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
		synchronized (syncObj)
		{
			return value != null;
		}
	}

	public void set(T value)
	{
		synchronized (syncObj)
		{
			this.value = value;
			syncObj.notifyAll();
		}
	}

}
