package pt.com.gcs.messaging;

public class ExpiredMessagesDeleter implements Runnable
{
	@Override
	public void run()
	{
		for (final QueueProcessor qp : QueueProcessorList.values())
		{
			qp.deleteExpiredMessages();
		}
	}
}
