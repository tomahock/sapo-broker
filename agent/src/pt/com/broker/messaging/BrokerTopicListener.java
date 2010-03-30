package pt.com.broker.messaging;

import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.net.BrokerProtocolHandler;
import pt.com.broker.types.ForwardResult;
import pt.com.broker.types.ListenerChannel;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.ForwardResult.Result;
import pt.com.broker.types.NetAction.DestinationType;

/**
 * BrokerTopicListener a represents local topic consumer.
 */
public class BrokerTopicListener extends BrokerListener
{
	private static final Logger log = LoggerFactory.getLogger(BrokerTopicListener.class);
	private static final ForwardResult failed = new ForwardResult(Result.FAILED);
	private static final ForwardResult success = new ForwardResult(Result.SUCCESS);

	private static final long MAX_WRITE_TIME = 125 * 1000 * 1000;
	private long droppedMessages;

	private boolean showSuspendedDeliveryMessage;
	private boolean showResumedDeliveryMessage;

	private long startDeliverAfter;

	public BrokerTopicListener(ListenerChannel lchannel, String destinationName)
	{
		super(lchannel, destinationName);
		droppedMessages = 0L;
		this.showSuspendedDeliveryMessage = false;
		this.showResumedDeliveryMessage = false;
		this.startDeliverAfter = System.nanoTime();
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

		try
		{
			// final NetMessage response = BrokerListener.buildNotification(amsg, getsubscriptionKey(), DestinationType.TOPIC);

			try
			{
				
				if (lchannel.isWritable())
				{
					if (showResumedDeliveryMessage)
					{
						String msg = String.format("Stopped discarding messages for topic '%s' and session '%s'. Dropped messages: %s", getsubscriptionKey(), lchannel.getRemoteAddressAsString(), droppedMessages);
						log.info(msg);
						droppedMessages = 0;
						showResumedDeliveryMessage = false;
					}

					lchannel.write(response);
					showSuspendedDeliveryMessage = true;
				}
				else
				{
					if (isReady())
					{
						ChannelFuture future = lchannel.write(response);
						final long writeStartTime = System.nanoTime();
						startDeliverAfter = writeStartTime + 10000;

						future.addListener(new ChannelFutureListener()
						{
							@Override
							public void operationComplete(ChannelFuture future) throws Exception
							{
								final long writeTime = System.nanoTime() - writeStartTime;

								if (writeTime >= MAX_WRITE_TIME)
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
							log.info("Started discarding messages for topic '{}' and session '{}'.", getsubscriptionKey(), lchannel.getRemoteAddressAsString());
							showSuspendedDeliveryMessage = false;
						}
						droppedMessages++;
					}
				}
			}
			catch (Throwable t)
			{
				try
				{
					((BrokerProtocolHandler) lchannel.getPipeline().get("broker-handler")).exceptionCaught(lchannel.getChannel(), t, null);
				}
				catch (Throwable t1)
				{
					log.error("Could not propagate error to the client session! Message: {}", t1.getMessage());
				}
			}

		}
		catch (Throwable e)
		{
			log.error("Error on message listener for '{}': {}", e.getMessage(), getsubscriptionKey());
		}
		return success;
	}

	@Override
	public boolean isReady()
	{
		return System.nanoTime() > startDeliverAfter;
	}

	@Override
	public boolean isActive()
	{
		return true;
	}
}
