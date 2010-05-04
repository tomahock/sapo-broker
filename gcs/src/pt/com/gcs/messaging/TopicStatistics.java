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
		long value = tDeliveredMessages.getAndSet(0);

		if(value == 0)
		{
			value = tDeliveredPublish.getAndSet(false) ? 0 : -1 ;
		}
		else
		{
			tDeliveredPublish.set(true);
		}
		return value;
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
		long value = tDiscardedMessages.getAndSet(0);

		if(value == 0)
		{
			value = tDiscardedPublish.getAndSet(false) ? 0 : -1 ;
		}
		else
		{
			tDiscardedPublish.set(true);
		}
		return value;
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
		long value = tTopicDispatchedToQueueMessages.getAndSet(0);

		if(value == 0)
		{
			value = tTopicDispatchedToQueuePublish.getAndSet(false) ? 0 : -1 ;
		}
		else
		{
			tTopicDispatchedToQueuePublish.set(true);
		}
		return value;
	}
}
