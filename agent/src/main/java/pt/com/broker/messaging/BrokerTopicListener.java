package pt.com.broker.messaging;

import java.util.concurrent.atomic.AtomicLong;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.auth.AccessControl;
import pt.com.broker.net.BrokerProtocolHandler;
import pt.com.broker.types.ForwardResult;
import pt.com.broker.types.ForwardResult.Result;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetNotification;
import pt.com.broker.types.channels.ListenerChannel;

/**
 * BrokerTopicListener a represents local topic consumer.
 */
public class BrokerTopicListener extends BrokerListener
{
	private static final Logger log = LoggerFactory.getLogger(BrokerTopicListener.class);
	private static final ForwardResult failed = new ForwardResult(Result.FAILED);
	private static final ForwardResult success = new ForwardResult(Result.SUCCESS);

	private static final long MAX_WRITE_TIME = 125 * 1000 * 1000;

	volatile private long droppedMessages;

	volatile private boolean showSuspendedDeliveryMessage;
	volatile private boolean showResumedDeliveryMessage;

	private AtomicLong startDeliverAfter;

	public BrokerTopicListener(ListenerChannel lchannel, String destinationName)
	{
		super(lchannel, destinationName);
		droppedMessages = 0L;
		this.showSuspendedDeliveryMessage = false;
		this.showResumedDeliveryMessage = false;
		this.startDeliverAfter = new AtomicLong(System.nanoTime());
	}

	@Override
	public boolean isAckRequired()
	{
		return false;
	}

	@Override
	public DestinationType getSourceDestinationType()
	{
		return DestinationType.TOPIC;
	}

	@Override
	public DestinationType getTargetDestinationType()
	{
		return DestinationType.TOPIC;
	}

	@Override
	protected ForwardResult doOnMessage(NetMessage response)
	{
		final ListenerChannel lchannel = getChannel();

		ForwardResult result = success;

		try
		{
			if (lchannel.isWritable())
			{
				getChannel().resetDeliveryTries();
				if (showResumedDeliveryMessage)
				{
					String msg = String.format("Stopped discarding messages for topic '%s' and session '%s'. Dropped messages: %s", getsubscriptionKey(), lchannel.getRemoteAddressAsString(), droppedMessages);
					log.info(msg);
					droppedMessages = 0;
					showResumedDeliveryMessage = false;
				}

				if (deliveryAllowed(response))
				{
					lchannel.write(response);
					showSuspendedDeliveryMessage = true;
				}
				else
				{
					return failed;
				}
			}
			else
			{
				if (isReady())
				{
					if (deliveryAllowed(response))
					{
						final ListenerChannel channel = getChannel();

						if (channel.getDeliveryTries() >= ListenerChannel.MAX_WRITE_TRIES)
						{
							++droppedMessages;
							return failed;
						}
						else
						{
							channel.incrementAndGetDeliveryTries();
							ChannelFuture future = lchannel.write(response);
							final long writeStartTime = System.nanoTime();
							startDeliverAfter.set(writeStartTime + 10000);

							future.addListener(new ChannelFutureListener()
							{
								@Override
								public void operationComplete(ChannelFuture future) throws Exception
								{
									channel.decrementAndGetDeliveryTries();

									final long writeTime = System.nanoTime() - writeStartTime;

									if (writeTime >= MAX_WRITE_TIME)
									{
										startDeliverAfter.set(System.nanoTime() + (writeTime / 2)); // suspend delivery for the same amount of time that the previous write took
									}
								}
							});
						}
					}
					else
					{
						// delivery not allowed
						return failed;
					}
				}
				else
				{
					// not writable and not ready - drop messages

					showResumedDeliveryMessage = true;

					if (showSuspendedDeliveryMessage)
					{
						log.info("Started discarding messages for topic '{}' and session '{}'.", getsubscriptionKey(), lchannel.getRemoteAddressAsString());
						showSuspendedDeliveryMessage = false;
					}
					++droppedMessages;
					result = failed;
				}
			}
		}
		catch (Throwable e)
		{
			log.error("Error on message listener for '{}': {}", e.getMessage(), getsubscriptionKey());
			try
			{
				((BrokerProtocolHandler) lchannel.getPipeline().get("broker-handler")).exceptionCaught(lchannel.getChannelContext(), e, null);
			}
			catch (Throwable t1)
			{
				log.error("Could not propagate error to the client session! Message: {}", t1.getMessage());
			}
		}
		return result;
	}

	private boolean deliveryAllowed(NetMessage response)
	{
		NetNotification notificationMessage = response.getAction().getNotificationMessage();
		Channel channel = this.getChannel().getChannel();

		DestinationType destinationType = DestinationType.TOPIC;
		return AccessControl.deliveryAllowed(response, destinationType, channel, this.getsubscriptionKey(), notificationMessage.getDestination());
	}

	@Override
	public boolean isReady()
	{
		return System.nanoTime() > startDeliverAfter.get();
	}

	@Override
	public boolean isActive()
	{
		return true;
	}
}
