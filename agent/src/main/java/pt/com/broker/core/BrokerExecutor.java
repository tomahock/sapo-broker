package pt.com.broker.core;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.caudexorigo.concurrent.CustomExecutors;

/**
 * BrokerExecutor provides execution services based on a ScheduledThreadPoolExecutor;
 */

public class BrokerExecutor
{
	private static final BrokerExecutor instance = new BrokerExecutor();

	private final ScheduledThreadPoolExecutor shed_exec_srv;

	private BrokerExecutor()
	{
		shed_exec_srv = CustomExecutors.newScheduledThreadPool(2, "Broker-Shed");
	}

	public static void scheduleAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit unit)
	{
		instance.shed_exec_srv.scheduleAtFixedRate(task, initialDelay, period, unit);
	}

	public static void scheduleWithFixedDelay(Runnable task, long initialDelay, long period, TimeUnit unit)
	{
		instance.shed_exec_srv.scheduleWithFixedDelay(task, initialDelay, period, unit);
	}

	public static void schedule(Runnable task, long delay, TimeUnit unit)
	{
		instance.shed_exec_srv.schedule(task, delay, unit);
	}
}