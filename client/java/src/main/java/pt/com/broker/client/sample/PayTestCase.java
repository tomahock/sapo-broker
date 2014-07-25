package pt.com.broker.client.sample;

import org.caudexorigo.concurrent.CustomExecutors;
import org.caudexorigo.concurrent.Sleep;

import java.util.concurrent.ExecutorService;

public class PayTestCase
{
	private static final long wait_time = 500;
	private static final int msgCount = 3;

	private static void createTrans(final long wait_time, final int msgCount, final String dname)
	{
		System.out.println("PayTestCase.createTrans: " + dname);
		PayConsumer pc = new PayConsumer("localhost", 3323, dname, wait_time, msgCount);
		PayProducer pp = new PayProducer("localhost", 3323, dname, wait_time, msgCount);
		pp.sendLoop(50);
	}

	public static void main(String[] args)
	{
		System.out.println("PayTestCase.main()");

		ExecutorService exec = CustomExecutors.newThreadPool(4000, "granel");

		long ix = 0l;
		while (true)
		{
			final String dname = String.format("/pay/trans-id/%s", ix++);

			Runnable run = new Runnable()
			{
				public void run()
				{
					createTrans(wait_time, msgCount, dname);
				}
			};
			exec.execute(run);
			Sleep.time(20);
		}
	}
}