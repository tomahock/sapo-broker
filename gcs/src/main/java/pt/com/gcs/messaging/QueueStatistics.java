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
		return getPublishInformation(qReceivedMessages, qReceivedPublish);
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
		return getPublishInformation(qDeliveredMessages, qDeliveredPublish);
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
		return getPublishInformation(qRedeliveredMessages, qRedeliveredPublish);
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
		return getPublishInformation(qExpiredMessages, qExpiredPublish);
	}

	private long getPublishInformation(AtomicLong value, AtomicBoolean status)
	{
		long retValue = value.getAndSet(0);

		if (retValue == 0)
		{
			retValue = status.getAndSet(false) ? 0 : -1;
		}
		else
		{
			status.set(true);
		}
		return retValue;
	}
}
