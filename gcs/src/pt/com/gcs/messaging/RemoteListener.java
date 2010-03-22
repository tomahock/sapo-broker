package pt.com.gcs.messaging;

import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.types.DeliverableMessage;
import pt.com.broker.types.ForwardResult;
import pt.com.broker.types.ListenerChannel;
import pt.com.broker.types.MessageListener;
import pt.com.broker.types.ForwardResult.Result;
import pt.com.broker.types.NetAction.DestinationType;

public class RemoteListener implements MessageListener
{
	private static final Logger log = LoggerFactory.getLogger(RemoteListener.class);
	private final ListenerChannel lchannel;
	private final String subscriptionKey;
	private final DestinationType sourceType;
	private final DestinationType targetType;

	private static final long QUEUE_MAX_WRITE_TIME = 250 * 1000 * 1000;
	private static final long TOPIC_MAX_WRITE_TIME = 125 * 1000 * 1000;

	private static final long RESERVE_TIME = 2 * 60 * 1000; // reserve for 2mn

	private static final ForwardResult failed = new ForwardResult(Result.FAILED);
	private static final ForwardResult successQueue = new ForwardResult(Result.SUCCESS, RESERVE_TIME);
	private static final ForwardResult successTopic = new ForwardResult(Result.SUCCESS);

	private final ForwardResult success;

	private final long max_write_time;

	private boolean showResumedDeliveryMessage;
	private boolean showSuspendedDeliveryMessage;
	private long startDeliverAfter;

	private long droppedMessages;

	public RemoteListener(ListenerChannel lchannel, String subscriptionKey, DestinationType sourceType, DestinationType targetType)
	{
		this.lchannel = lchannel;
		this.subscriptionKey = subscriptionKey;
		this.sourceType = sourceType;
		this.targetType = targetType;

		this.showResumedDeliveryMessage = false;
		this.showSuspendedDeliveryMessage = false;
		this.startDeliverAfter = System.nanoTime();

		if (targetType == DestinationType.QUEUE)
		{
			max_write_time = QUEUE_MAX_WRITE_TIME;
			success = successQueue;
		}
		else
		{
			max_write_time = TOPIC_MAX_WRITE_TIME;
			success = successTopic;
		}

		droppedMessages = 0L;
	}

	@Override
	public ListenerChannel getChannel()
	{
		return lchannel;
	}

	@Override
	public String getsubscriptionKey()
	{
		return subscriptionKey;
	}

	@Override
	public DestinationType getSourceDestinationType()
	{
		return sourceType;
	}

	@Override
	public DestinationType getTargetDestinationType()
	{
		return targetType;
	}

	@Override
	public boolean isAckRequired()
	{
		return true;
	}

	@Override
	public boolean isActive()
	{
		return true;
	}

	@Override
	public Type getType()
	{
		return MessageListener.Type.REMOTE;
	}

	@Override
	public boolean isReady()
	{
		return System.nanoTime() > startDeliverAfter;
	}

	@Override
	public ForwardResult onMessage(DeliverableMessage message)
	{
		if (message == null)
			return failed;
		
		if (!(message instanceof InternalMessage))
		{
			log.warn("Don't know how to handle this message type");
			return failed;			
		}

		final ListenerChannel lchannel = getChannel();

		try
		{

			if (lchannel.isWritable())
			{
				if (showResumedDeliveryMessage)
				{
					if (targetType == DestinationType.TOPIC)
					{
						String msg = String.format("Stopped discarding messages for topic '%s' and session '%s'. Dropped messages: %s", getsubscriptionKey(), lchannel.getRemoteAddressAsString(), droppedMessages);
						log.info(msg);
						droppedMessages = 0;
					}
					else if (targetType == DestinationType.QUEUE)
					{
						log.info(String.format("Resume message delivery for queue '%s' to session '%s'.", getsubscriptionKey(), lchannel.getRemoteAddressAsString()));
					}

					showResumedDeliveryMessage = false;
				}

				lchannel.write(message);
				showSuspendedDeliveryMessage = true;
			}
			else
			{

				if (isReady())
				{
					ChannelFuture future = lchannel.write(message);
					final long writeStartTime = System.nanoTime();
					startDeliverAfter = writeStartTime + 2500;

					future.addListener(new ChannelFutureListener()
					{
						@Override
						public void operationComplete(ChannelFuture future) throws Exception
						{
							final long writeTime = System.nanoTime() - writeStartTime;

							if (writeTime >= max_write_time)
							{
								startDeliverAfter = System.nanoTime() + (writeTime / 2); // suspend delivery for the same amount of time that the previous write took.;
							}
						}
					});
				}
				else
				{
					showResumedDeliveryMessage = true;

					if (showSuspendedDeliveryMessage)
					{

						if (targetType == DestinationType.TOPIC)
						{
							log.info("Started discarding messages for topic '{}' and session '{}'.", getsubscriptionKey(), lchannel.getRemoteAddressAsString());
						}
						else if (targetType == DestinationType.QUEUE)
						{
							log.info(String.format("Suspending message delivery for queue '%s' to session '%s'.", getsubscriptionKey(), lchannel.getRemoteAddressAsString()));
						}

						showSuspendedDeliveryMessage = false;
					}

					if (targetType == DestinationType.TOPIC)
					{
						droppedMessages++;
					}
				}
			}

			return success;
		}
		catch (Throwable ct)
		{
			log.error(ct.getMessage(), ct);
			try
			{
				lchannel.close();
			}
			catch (Throwable ict)
			{
				log.error(ict.getMessage(), ict);
			}
		}

		return failed;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((lchannel == null) ? 0 : lchannel.hashCode());
		result = prime * result + ((sourceType == null) ? 0 : sourceType.hashCode());
		result = prime * result + ((subscriptionKey == null) ? 0 : subscriptionKey.hashCode());
		result = prime * result + ((targetType == null) ? 0 : targetType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RemoteListener other = (RemoteListener) obj;
		if (lchannel == null)
		{
			if (other.lchannel != null)
				return false;
		}
		else if (!lchannel.equals(other.lchannel))
			return false;
		if (sourceType == null)
		{
			if (other.sourceType != null)
				return false;
		}
		else if (!sourceType.equals(other.sourceType))
			return false;
		if (subscriptionKey == null)
		{
			if (other.subscriptionKey != null)
				return false;
		}
		else if (!subscriptionKey.equals(other.subscriptionKey))
			return false;
		if (targetType == null)
		{
			if (other.targetType != null)
				return false;
		}
		else if (!targetType.equals(other.targetType))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return "RemoteListener [type=" + getType().toString() + ", lchannel=" + lchannel + ", subscriptionKey=" + subscriptionKey + "]";
	}
}