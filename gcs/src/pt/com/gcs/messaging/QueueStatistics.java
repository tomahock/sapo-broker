package pt.com.gcs.messaging;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class QueueStatistics
{
	private final AtomicLong qReceivedMessages = new AtomicLong(0);
	private final AtomicBoolean qReceivedPublish = new AtomicBoolean(true);
	public final void newQueueMessageReceived()
	{
		qReceivedMessages.incrementAndGet();
	}
	public long getQueueMessagesReceivedAndReset()
	{
		long value = qReceivedMessages.getAndSet(0);

		if(value == 0)
		{
			value = qReceivedPublish.getAndSet(false) ? 0 : -1 ;
		}
		else
		{
			qReceivedPublish.set(true);
		}
		return value;
	}
	// delivered
	private final AtomicLong qDeliveredMessages = new AtomicLong(0);
	private final AtomicBoolean qDeliveredPublish = new AtomicBoolean(true);
	public final void newQueueMessageDelivered()
	{
		qDeliveredMessages.incrementAndGet();
	}
	public long getQueueMessagesDeliveredAndReset()
	{
		long value = qDeliveredMessages.getAndSet(0);

		if(value == 0)
		{
			value = qDeliveredPublish.getAndSet(false) ? 0 : -1 ;
		}
		else
		{
			qDeliveredPublish.set(true);
		}
		return value;
	}
	
	// redelivered
	private final AtomicLong qRedeliveredMessages = new AtomicLong(0);
	private final AtomicBoolean qRedeliveredPublish = new AtomicBoolean(true);
	public final void newQueueRedeliveredMessage()
	{
		qRedeliveredMessages.incrementAndGet();
	}
	public final long getQueueMessagesRedeliveredAndReset()
	{
		long value = qRedeliveredMessages.getAndSet(0);

		if(value == 0)
		{
			value = qRedeliveredPublish.getAndSet(false) ? 0 : -1 ;
		}
		else
		{
			qRedeliveredPublish.set(true);
		}
		return value;
	}
	// failed
	private final AtomicLong qFailedMessages = new AtomicLong(0);
	private final AtomicBoolean qFailedPublish = new AtomicBoolean(true);
	public final void newQueueFailedMessage()
	{
		qFailedMessages.incrementAndGet();
	}
	public final long getQueueMessagesFailedAndReset()
	{
		long value = qFailedMessages.getAndSet(0);

		if(value == 0)
		{
			value = qFailedPublish.getAndSet(false) ? 0 : -1 ;
		}
		else
		{
			qFailedPublish.set(true);
		}
		return value;
	}
	
	// expired
	private final AtomicLong qExpiredMessages = new AtomicLong(0);
	private final AtomicBoolean qExpiredPublish = new AtomicBoolean(true);
	public final void newQueueExpiredMessage()
	{
		qExpiredMessages.incrementAndGet();
	}
	public final long getQueueMessagesExpiredAndReset()
	{
		long value = qExpiredMessages.getAndSet(0);

		if(value == 0)
		{
			value = qExpiredPublish.getAndSet(false) ? 0 : -1 ;
		}
		else
		{
			qExpiredPublish.set(true);
		}
		return value;
	}
}
