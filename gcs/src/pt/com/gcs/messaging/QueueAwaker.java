package pt.com.gcs.messaging;

import org.caudexorigo.concurrent.Sleep;

/**
 * QueueAwaker is a Runnable type responsible form initialize the queue message deliver process.
 * 
 */

class QueueAwaker implements Runnable
{
	@Override
	public void run()
	{
		for (final QueueProcessor qp : QueueProcessorList.values())
		{
			Runnable awaker = new Runnable()
			{
				public void run()
				{
					qp.wakeup();
				}
			};

			GcsExecutor.execute(awaker);
			Sleep.time(100);
		}
	}

}
