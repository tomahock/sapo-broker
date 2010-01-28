package pt.com.broker.messaging;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.types.NetFault;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.gcs.messaging.GcsExecutor;
import pt.com.gcs.messaging.InternalMessage;
import pt.com.gcs.messaging.LocalQueueConsumers;
import pt.com.gcs.messaging.MessageListener;
import pt.com.gcs.messaging.QueueProcessor;
import pt.com.gcs.messaging.QueueProcessorList;
import pt.com.gcs.messaging.QueueProcessorList.MaximumQueuesAllowedReachedException;

/*
 * SynchronousMessageListener represents a poll request by a client. 
 */
public class SynchronousMessageListener implements MessageListener
{
	private static final Logger log = LoggerFactory.getLogger(SynchronousMessageListener.class);

	private static final String SESSION_ATT_PREFIX = "SYNC_MESSAGE_LISTENER#";

	private static final long ACTIVE_INTERVAL = 5 * 60 * 1000; // 5mn

	private AtomicBoolean ready;
	private final String queueName;
	private final IoSession ioSession;

	private volatile long expires;
	private volatile boolean inNoWaitMode;
	private volatile String actionId;

	private AtomicLong lastDeliveredMessage = new AtomicLong(0);

	public SynchronousMessageListener(String queueName, IoSession session)
	{
		this.ready = new AtomicBoolean(false);
		this.queueName = queueName;
		this.ioSession = session;
		this.setInNoWaitMode(false);
	}

	@Override
	public String getDestinationName()
	{
		return queueName;
	}

	@Override
	public DestinationType getSourceDestinationType()
	{
		return DestinationType.QUEUE;
	}

	@Override
	public DestinationType getTargetDestinationType()
	{
		return DestinationType.QUEUE;
	}

	@Override
	public long onMessage(InternalMessage message)
	{
		if (!ready.get())
		{
			log.error("We shouldn't be here. A SynchronousMessageListener should not be called when in a 'not ready' state.");
			return -1;
		}

		ready.set(false);

		if ((ioSession != null) && ioSession.isConnected() && !ioSession.isClosing())
		{
			final NetMessage response = BrokerListener.buildNotification(message, getDestinationName(), getSourceDestinationType());
			ioSession.write(response);
			lastDeliveredMessage.set(System.currentTimeMillis());
		}
		else
		{
			LocalQueueConsumers.remove(this);
			return -1;
		}

		return 15 * 60 * 1000; // reserve for 15mn
	}

	@Override
	public boolean ready()
	{
		return ready.get();
	}

	public void activate(long timeout, String actionId)
	{
		this.actionId = actionId;
		activate(timeout);
	}

	public void activate(long timeout)
	{
		if (timeout == 0)
		{
			// wait for ever

			this.setExpires(Long.MAX_VALUE);

			ready.set(true);
			return;
		}

		if (timeout < 0)
		{
			boolean noMessages = false;

			setInNoWaitMode(true);

			QueueProcessor queueProcessor;
			try
			{
				queueProcessor = QueueProcessorList.get(getDestinationName());
				if (queueProcessor.getQueuedMessagesCount() == 0)
				{
					noMessages = true;
				}
			}
			catch (MaximumQueuesAllowedReachedException e)
			{
				noMessages = true;
			}
			if (noMessages)
			{
				NetMessage faultMsg = NetFault.getMessageFaultWithDetail(NetFault.NoMessageInQueueErrorMessage, getDestinationName());
				if (actionId != null)
				{
					faultMsg.getAction().getFaultMessage().setActionId(actionId);
				}
				if ((ioSession != null) && ioSession.isConnected() && !ioSession.isClosing())
				{
					ioSession.write(faultMsg);
				}

				ready.set(false);
				setInNoWaitMode(false);
				return;
			}
			// There is, at least one message. That is no guarantee that the sync client will receive it, so set a timeout of one second and set mode to no wait (inNoWaitMode)

			timeout = 1000;
		}

		this.setExpires(System.currentTimeMillis() + timeout);
		ready.set(true);

		GcsExecutor.schedule(new Runnable()
		{
			@Override
			public void run()
			{
				notifyTimeout();
			}

		}, timeout, TimeUnit.MILLISECONDS);
	}

	public void notifyTimeout()
	{
		if ((System.currentTimeMillis() >= getExpires()) && ready.get())
		{

			ready.set(false);

			NetMessage faultMsg = null;

			if (isInNoWaitMode())
			{
				faultMsg = NetFault.getMessageFaultWithDetail(NetFault.NoMessageInQueueErrorMessage, getDestinationName());
			}
			else
			{
				faultMsg = NetFault.getMessageFaultWithDetail(NetFault.PollTimeoutErrorMessage, getDestinationName());
			}

			if (actionId != null)
			{
				faultMsg.getAction().getFaultMessage().setActionId(actionId);
			}
			if ((ioSession != null) && ioSession.isConnected() && !ioSession.isClosing())
			{
				ioSession.write(faultMsg);
			}
		}
	}

	public static String getComposedQueueName(String queueName)
	{
		return SESSION_ATT_PREFIX + queueName;
	}

	public static void removeSession(IoSession session)
	{
		Set<Object> attributeKeys = session.getAttributeKeys();
		for (Object attributeKey : attributeKeys)
		{
			if (attributeKey.toString().startsWith(SESSION_ATT_PREFIX))
			{
				Object attributeValue = session.getAttribute(attributeKey);
				if (attributeValue instanceof SynchronousMessageListener)
				{
					SynchronousMessageListener listener = (SynchronousMessageListener) attributeValue;
					BrokerSyncConsumer.pollStoped(listener.getDestinationName());
					LocalQueueConsumers.remove(listener);
				}
			}
		}
	}

	private void setInNoWaitMode(boolean inNoWaitMode)
	{
		synchronized (this)
		{
			this.inNoWaitMode = inNoWaitMode;
		}
	}

	private boolean isInNoWaitMode()
	{
		synchronized (this)
		{
			return inNoWaitMode;
		}
	}

	private void setExpires(long expires)
	{
		synchronized (this)
		{
			this.expires = expires;
		}
	}

	private long getExpires()
	{
		synchronized (this)
		{
			return expires;
		}
	}

	@Override
	public boolean isActive()
	{
		return (lastDeliveredMessage.get() + ACTIVE_INTERVAL) >= System.currentTimeMillis();
	}

}
