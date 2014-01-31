package pt.com.broker.messaging;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.auth.AccessControl;
import pt.com.broker.types.ForwardResult;
import pt.com.broker.types.ForwardResult.Result;
import pt.com.broker.types.MessageListener;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.broker.types.NetFault;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.channels.ListenerChannel;
import pt.com.gcs.messaging.GcsExecutor;
import pt.com.gcs.messaging.QueueProcessor;
import pt.com.gcs.messaging.QueueProcessorList;

/*
 * SynchronousMessageListener represents a poll request by a client. 
 */
public class SynchronousMessageListener extends BrokerListener
{
	private static final Logger log = LoggerFactory.getLogger(SynchronousMessageListener.class);

	private static final long ACTIVE_INTERVAL = 5 * 60 * 1000; // 5mn
	private static final long DEFAULT_RESERVE_TIME = 15 * 60 * 1000; // reserve for 15mn

	private static final ForwardResult FAILED = new ForwardResult(Result.FAILED);

	private AtomicBoolean isReady;
	private final String queueName;

	private volatile long expires;
	private volatile boolean inNoWaitMode;
	private volatile String actionId;

	private AtomicLong lastDeliveredMessage = new AtomicLong(System.currentTimeMillis());

	private ForwardResult sucess;

	public SynchronousMessageListener(ListenerChannel lchannel, String queueName)
	{
		super(lchannel, queueName);

		sucess = new ForwardResult(Result.SUCCESS, DEFAULT_RESERVE_TIME);

		this.queueName = queueName;
		this.setInNoWaitMode(false);
		this.isReady = new AtomicBoolean(false);
	}

	@Override
	public String getsubscriptionKey()
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
	protected ForwardResult doOnMessage(NetMessage response)
	{
		if (!isReady.get())
		{
			log.error("We shouldn't be here. A SynchronousMessageListener should not be called when in a 'not ready' state.");
			return FAILED;
		}

		setReady(false);

		final ListenerChannel lchannel = getChannel();

		if ((lchannel != null) && lchannel.isConnected() && lchannel.isWritable())
		{
			// final NetMessage response = BrokerListener.buildNotification(message, queueName, getSourceDestinationType());

			if (deliveryAllowed(response, lchannel.getChannel()))
			{
				lchannel.write(response);
				lastDeliveredMessage.set(System.currentTimeMillis());
			}
			else
			{
				return failed;
			}
		}
		else
		{
			if ((lchannel == null) || !lchannel.isConnected())
			{
				QueueProcessorList.removeListener(this);
			}
			return FAILED;
		}

		return sucess;
	}

	private void setReady(boolean ready)
	{
		isReady.set(ready);
		onEventChange(ready ? MessageListenerState.Ready : MessageListenerState.NotReady);
	}

	@Override
	public boolean isReady()
	{
		return isReady.get();
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

			setReady(true);
			return;
		}

		if (timeout < 0)
		{
			boolean noMessages = false;

			setInNoWaitMode(true);

			QueueProcessor queueProcessor = QueueProcessorList.get(getsubscriptionKey());
			if (queueProcessor == null)
			{
				noMessages = true;
				return;
			}
			if (queueProcessor.getQueuedMessagesCount() == 0)
			{
				noMessages = true;
			}

			if (noMessages)
			{
				final ListenerChannel lchannel = getChannel();

				NetMessage faultMsg = NetFault.getMessageFaultWithDetail(NetFault.NoMessageInQueueErrorMessage, getsubscriptionKey());
				if (actionId != null)
				{
					faultMsg.getAction().getFaultMessage().setActionId(actionId);
				}

				if ((lchannel != null) && lchannel.isConnected() && lchannel.isWritable())
				{
					lchannel.write(faultMsg);
				}

				setReady(false);
				setInNoWaitMode(false);
				return;
			}
			// There is, at least one message. That is no guarantee that the sync client will receive it, so set a timeout of one second and set mode to no wait (inNoWaitMode)

			timeout = 1000;
		}

		this.setExpires(System.currentTimeMillis() + timeout);
		setReady(true);

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
		if ((System.currentTimeMillis() >= getExpires()) && isReady.get())
		{
			setReady(false);

			NetMessage faultMsg = null;

			if (isInNoWaitMode())
			{
				faultMsg = NetFault.getMessageFaultWithDetail(NetFault.NoMessageInQueueErrorMessage, getsubscriptionKey());
			}
			else
			{
				faultMsg = NetFault.getMessageFaultWithDetail(NetFault.PollTimeoutErrorMessage, getsubscriptionKey());
			}

			if (actionId != null)
			{
				faultMsg.getAction().getFaultMessage().setActionId(actionId);
			}
			final ListenerChannel lchannel = getChannel();
			if ((lchannel != null) && lchannel.isConnected() && lchannel.isWritable())
			{
				lchannel.write(faultMsg);
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
		if (isReady())
			return true;

		return (lastDeliveredMessage.get() + ACTIVE_INTERVAL) >= System.currentTimeMillis();
	}

	@Override
	public boolean isAckRequired()
	{
		return true;
	}

	@Override
	public Type getType()
	{
		return MessageListener.Type.LOCAL;
	}

	@Override
	public String toString()
	{
		return "SynchronousMessageListener [type=" + getType().toString() + ", lchannel=" + getChannel() + ", queueName=" + queueName + "]";
	}

	public void setReserveTime(long reserveTime)
	{
		this.sucess.time = reserveTime;
	}

	/**
	 * If the message was original sent to a topic validate delivery.
	 */
	private boolean deliveryAllowed(NetMessage response, Channel channel)
	{
		String originalDestination = response.getHeaders().get("ORIGINAL_DESTINATION");
		if (originalDestination == null)
		{
			return true;
		}

		// This is a Virtual Queue
		DestinationType destinationType = DestinationType.TOPIC;
		return AccessControl.deliveryAllowed(response, destinationType, channel, this.getsubscriptionKey(), originalDestination);
	}
}
