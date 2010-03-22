package pt.com.broker.messaging;

import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.net.BrokerProtocolHandler;
import pt.com.broker.types.DeliverableMessage;
import pt.com.broker.types.ForwardResult;
import pt.com.broker.types.ListenerChannel;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.ForwardResult.Result;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.gcs.messaging.Gcs;

/**
 * BrokerQueueListener represents a local queue consumer.
 */
public class BrokerQueueListener extends BrokerListener
{
	private static final Logger log = LoggerFactory.getLogger(BrokerQueueListener.class);

	private static final long MAX_WRITE_TIME = 250 * 1000 * 1000;
	private static final long RESERVE_TIME = 2 * 60 * 1000; // reserve for 2mn
	private static final String ACK_REQUIRED = "ACK_REQUIRED";

	private static final ForwardResult success = new ForwardResult(Result.SUCCESS, RESERVE_TIME);
	private static final ForwardResult ackNotRequired = new ForwardResult(Result.NOT_ACKNOWLEDGE);
	private final boolean ackRequired;

	private boolean showResumedDeliveryMessage;
	private boolean showSuspendedDeliveryMessage;

	private long startDeliverAfter;

	public BrokerQueueListener(ListenerChannel lchannel, String destinationName, boolean ackRequired)
	{
		super(lchannel, destinationName);
		this.ackRequired = ackRequired;
		this.showResumedDeliveryMessage = false;
		this.showSuspendedDeliveryMessage = false;
		this.startDeliverAfter = System.nanoTime();
	}

	@Override
	public boolean isAckRequired()
	{
		return ackRequired;
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
		final ListenerChannel lchannel = getChannel();

		try
		{
			//final NetMessage response = BrokerListener.buildNotification(msg, getsubscriptionKey(), pt.com.broker.types.NetAction.DestinationType.QUEUE);

			if (!isAckRequired())
			{
				response.getHeaders().put(ACK_REQUIRED, "false");
			}

			if (lchannel.isWritable())
			{
				if (showResumedDeliveryMessage)
				{
					log.info(String.format("Resume message delivery for queue '%s' to session '%s'.", getsubscriptionKey(), lchannel.getRemoteAddressAsString()));
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
					startDeliverAfter = writeStartTime + 1000000;

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
						log.info(String.format("Suspending message delivery for queue '%s' to session '%s'.", getsubscriptionKey(), lchannel.getRemoteAddressAsString()));
						showSuspendedDeliveryMessage = false;
					}
				}
			}

			if (isAckRequired())
				return success;

			return ackNotRequired;
		}
		catch (Throwable e)
		{
			if (e instanceof org.jibx.runtime.JiBXException)
			{
				try
				{
					String mid = response.getAction().getNotificationMessage().getMessage().getMessageId();
					Gcs.ackMessage(getsubscriptionKey(), mid);
					log.warn("Undeliverable message was deleted. Id: '{}'", mid);
				}
				catch (Throwable tx)
				{
					// ignore
				}
			}
			try
			{
				((BrokerProtocolHandler) lchannel.getPipeline().get("broker-handler")).exceptionCaught(lchannel.getChannel(), e, null);
			}
			catch (Throwable t)
			{
				log.error(t.getMessage(), t);
			}
		}

		return failed;
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
