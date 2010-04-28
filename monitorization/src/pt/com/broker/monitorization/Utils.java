package pt.com.broker.monitorization;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.caudexorigo.concurrent.CustomExecutors;

public class Utils
{
	private static ScheduledThreadPoolExecutor scheduledThreadPool;
	static {
		scheduledThreadPool = CustomExecutors.newScheduledThreadPool(4, "Monitor-Sched");
	}
	
	public static void schedule(Runnable task, long initialDelay, long delay, TimeUnit unit)
	{
		scheduledThreadPool.scheduleWithFixedDelay(task, initialDelay, delay, unit);
	}
	public static void execute(Runnable task)
	{
		scheduledThreadPool.execute(task);
	}
}
