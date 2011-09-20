package pt.com.broker.functests.simulation.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.caudexorigo.concurrent.Sleep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.client.BrokerClient;
import pt.com.broker.client.HostInfo;
import pt.com.broker.client.messaging.BrokerListener;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.broker.types.NetNotification;
import pt.com.broker.types.NetProtocolType;
import pt.com.broker.types.NetSubscribe;
import pt.com.gcs.messaging.GcsExecutor;

public class Consumers
{

	private static final Logger log = LoggerFactory.getLogger(Consumers.class);

	private final int counsumersCount;
	private final long ackDelay;
	private final HostInfo hostInfo;
	private final long subscriptionDuration;
	private final long unsubscribedDuration;
	private final NetProtocolType protocolType;
	private final String appName;

	private List<ConsumerInfo> consumers;
	private final String destinationName;

	private NetSubscribe subscribeMsg;

	private final DestinationType destinationType;

	private static List<Consumers> activeConsumers = new ArrayList<Consumers>();

	static
	{
		final long interval = 15;

		Runnable command = new Runnable()
		{

			@Override
			public void run()
			{
				StringBuffer sb = new StringBuffer();

				for (Consumers consumers : activeConsumers)
				{
					sb.append("\nConsumers:");
					sb.append(consumers.appName);
					sb.append("\n");
					for (ConsumerInfo ci : consumers.consumers)
					{
						sb.append(String.format("Messages received in the last %s (s) by %s: %s\n", interval, ci.consumerName, ci.messagesReceived));
						ci.messagesReceived = 0;
					}
				}

				System.out.println(sb.toString());
			}
		};

		GcsExecutor.scheduleAtFixedRate(command, interval, interval, TimeUnit.SECONDS);

	}

	private static class ConsumerInfo
	{
		public enum SubscriptionAction
		{
			Subscribe, Unsubscribe
		};

		public BrokerClient brokerClient;
		public SubscriptionAction nextAction;
		public long actionTime = Long.MAX_VALUE;
		public String consumerName;
		public volatile long messagesReceived = 0;

		public BrokerListener listener;
	}

	public Consumers(DestinationType destinationType, int counsumersCount, String destinationName, long ackDelay, long subscriptionDuration, long unsubscribedDuration, HostInfo hostInfo, NetProtocolType protocolType, String appName)
	{
		this.destinationType = destinationType;
		this.counsumersCount = counsumersCount;
		this.destinationName = destinationName;
		this.ackDelay = ackDelay;
		this.subscriptionDuration = subscriptionDuration;
		this.unsubscribedDuration = unsubscribedDuration;
		this.hostInfo = hostInfo;
		this.protocolType = protocolType;
		this.appName = appName;
	}

	public void init()
	{
		consumers = new ArrayList<ConsumerInfo>(counsumersCount);
		for (int i = 0; i != counsumersCount; ++i)
		{
			ConsumerInfo ci = new ConsumerInfo();
			try
			{
				ci.consumerName = appName + i;
				ci.brokerClient = new BrokerClient(hostInfo.getHostname(), hostInfo.getPort(), String.format("Consumer:%s:%s", destinationName, i), protocolType);

				consumers.add(ci);
			}
			catch (Throwable e)
			{
				e.printStackTrace();
			}
		}
	}

	public void start()
	{
		subscribeMsg = new NetSubscribe(destinationName, destinationType);

		Random random = new Random(System.currentTimeMillis());

		for (ConsumerInfo ci : consumers)
		{
			final ConsumerInfo consumerInfo = ci;

			BrokerListener listener = new BrokerListener()
			{
				@Override
				public void onMessage(NetNotification message)
				{
					++consumerInfo.messagesReceived;
					if (ackDelay != 0)
					{
						Sleep.time(ackDelay);
					}
					try
					{
						if (destinationType != DestinationType.TOPIC)
						{
							consumerInfo.brokerClient.acknowledge(message);
						}
					}
					catch (Throwable e)
					{
						e.printStackTrace();
					}
				}

				@Override
				public boolean isAutoAck()
				{
					return false;
				}
			};

			try
			{
				ci.brokerClient.addAsyncConsumer(subscribeMsg, listener);
				ci.listener = listener;

				if (subscriptionDuration != 0 && unsubscribedDuration != 0)
				{
					ci.nextAction = ConsumerInfo.SubscriptionAction.Unsubscribe;
					ci.actionTime = System.currentTimeMillis() + (random.nextLong() % subscriptionDuration);
				}
			}
			catch (Throwable e)
			{
				e.printStackTrace();
			}
		}
		if (subscriptionDuration != 0 && unsubscribedDuration != 0)
		{
			Runnable command = new Runnable()
			{
				@Override
				public void run()
				{
					long now = System.currentTimeMillis();

					for (ConsumerInfo ci : consumers)
					{
						if (ci.actionTime < now)
						{
							if (ci.nextAction == ConsumerInfo.SubscriptionAction.Subscribe)
							{
								try
								{
									ci.brokerClient.addAsyncConsumer(subscribeMsg, ci.listener);
									ci.nextAction = ConsumerInfo.SubscriptionAction.Unsubscribe;
									ci.actionTime = now + subscriptionDuration;
								}
								catch (Throwable e)
								{
									log.error("Failed to subscribe", e);
								}
							}
							else
							{
								try
								{
									ci.brokerClient.unsubscribe(DestinationType.VIRTUAL_QUEUE, destinationName);
									ci.nextAction = ConsumerInfo.SubscriptionAction.Subscribe;
									ci.actionTime = now + unsubscribedDuration;
								}
								catch (Throwable e)
								{
									log.error("Failed to unsubscribe", e);
								}

							}
						}
					}

				}
			};

			GcsExecutor.scheduleAtFixedRate(command, 1, 1, TimeUnit.SECONDS);
		}
		activeConsumers.add(this);
	}

	public void end()
	{
		for (ConsumerInfo ci : consumers)
		{
			ci.brokerClient.close();
		}
		consumers.clear();
		activeConsumers.remove(this);
	}
}
