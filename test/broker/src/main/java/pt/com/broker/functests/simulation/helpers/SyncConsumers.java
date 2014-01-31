package pt.com.broker.functests.simulation.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.caudexorigo.concurrent.CustomExecutors;
import org.caudexorigo.concurrent.Sleep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.client.BrokerClient;
import pt.com.broker.client.HostInfo;
import pt.com.broker.types.NetNotification;
import pt.com.broker.types.NetProtocolType;
import pt.com.gcs.messaging.GcsExecutor;

public class SyncConsumers
{

	private static final Logger log = LoggerFactory.getLogger(SyncConsumers.class);

	private final int consumersCount;
	private final String queueName;
	private final long ackDelay;
	private final long pollInterval;
	private final HostInfo hostInfo;
	private final NetProtocolType protocolType;

	private List<ConsumerInfo> consumers;

	private ScheduledThreadPoolExecutor shed_exec_srv;

	private static List<SyncConsumers> activeConsumers = new ArrayList<SyncConsumers>();

	private final String appName;

	static
	{
		final long interval = 15;

		Runnable command = new Runnable()
		{
			@Override
			public void run()
			{
				StringBuffer sb = new StringBuffer();

				for (SyncConsumers consumers : activeConsumers)
				{
					sb.append("\nSync Consumers:");
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

	private class ConsumerInfo
	{
		public BrokerClient brokerClient;
		public String consumerName;
		public volatile long messagesReceived = 0;
	}

	public SyncConsumers(int consumersCount, String queueName, long ackDelay, long pollInterval, HostInfo hostInfo, NetProtocolType protocolType, String appName)
	{
		this.consumersCount = consumersCount;
		this.queueName = queueName;
		this.ackDelay = ackDelay;
		this.pollInterval = pollInterval;
		this.hostInfo = hostInfo;
		this.protocolType = protocolType;
		this.appName = appName;

		shed_exec_srv = CustomExecutors.newScheduledThreadPool(consumersCount, "SyncConsumers-Sched");
	}

	public void init()
	{
		consumers = new ArrayList<ConsumerInfo>(consumersCount);
		for (int i = 0; i != consumersCount; ++i)
		{
			ConsumerInfo ci = new ConsumerInfo();
			try
			{
				ci.consumerName = appName + i;
				ci.brokerClient = new BrokerClient(hostInfo.getHostname(), hostInfo.getPort(), String.format("Consumer:%s:%s", queueName, i), protocolType);

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
		for (ConsumerInfo ci : consumers)
		{

			final ConsumerInfo consumerInfo = ci;

			shed_exec_srv.execute(new Runnable()
			{
				@Override
				public void run()
				{
					while (true)
					{
						try
						{
							NetNotification notification = consumerInfo.brokerClient.poll(queueName);
							++consumerInfo.messagesReceived;
							Sleep.time(ackDelay);
							consumerInfo.brokerClient.acknowledge(notification);
							if (pollInterval != 0)
							{
								Sleep.time(pollInterval);
							}
						}
						catch (Throwable e)
						{
							log.error("Failed to poll", e);
						}
					}
				}
			});
		}
		activeConsumers.add(this);
	}

	public void end()
	{
		for (ConsumerInfo ci : consumers)
		{
			ci.brokerClient.close();
		}

		activeConsumers.remove(this);
	}
}
