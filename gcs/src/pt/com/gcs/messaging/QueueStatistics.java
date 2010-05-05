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
		return getPublishInformationInformation(qReceivedMessages, qReceivedPublish);
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
		return getPublishInformationInformation(qDeliveredMessages, qDeliveredPublish);
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
		return getPublishInformationInformation(qRedeliveredMessages, qRedeliveredPublish);
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
		return getPublishInformationInformation(qFailedMessages, qFailedPublish);
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
		return getPublishInformationInformation(qExpiredMessages, qExpiredPublish);
	}
	
	private long getPublishInformationInformation(AtomicLong value, AtomicBoolean status)
	{
		long retValue = value.getAndSet(0);

		if(retValue == 0)
		{
			retValue = status.getAndSet(false) ? 0 : -1 ;
		}
		else
		{
			status.set(true);
		}
		return retValue;
	}
}
