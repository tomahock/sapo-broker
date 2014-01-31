package pt.com.gcs.messaging;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class TopicStatistics
{
	// delivered
	private final AtomicLong tDeliveredMessages = new AtomicLong(0);
	private final AtomicBoolean tDeliveredPublish = new AtomicBoolean(true);

	public final void newTopicMessageDelivered()
	{
		tDeliveredMessages.incrementAndGet();
	}

	public long getTopicMessagesDeliveredAndReset()
	{
		return getPublishInformationInformation(tDeliveredMessages, tDeliveredPublish);
	}

	// discarded
	private final AtomicLong tDiscardedMessages = new AtomicLong(0);
	private final AtomicBoolean tDiscardedPublish = new AtomicBoolean(true);

	public final void newTopicDiscardedMessage()
	{
		tDiscardedMessages.incrementAndGet();
	}

	public final long getTopicMessagesDiscardedAndReset()
	{
		return getPublishInformationInformation(tDiscardedMessages, tDiscardedPublish);
	}

	// dispatched to queue
	private final AtomicLong tTopicDispatchedToQueueMessages = new AtomicLong(0);
	private final AtomicBoolean tTopicDispatchedToQueuePublish = new AtomicBoolean(true);

	public final void newTopicDispatchedToQueueMessage()
	{
		tTopicDispatchedToQueueMessages.incrementAndGet();
	}

	public final long getTopicMessagesDispatchedToQueueAndReset()
	{
		return getPublishInformationInformation(tTopicDispatchedToQueueMessages, tTopicDispatchedToQueuePublish);
	}

	private long getPublishInformationInformation(AtomicLong value, AtomicBoolean status)
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
