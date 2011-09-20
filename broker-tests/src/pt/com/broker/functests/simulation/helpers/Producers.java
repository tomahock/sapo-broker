package pt.com.broker.functests.simulation.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.caudexorigo.concurrent.CustomExecutors;
import org.caudexorigo.concurrent.Sleep;
import org.caudexorigo.text.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.client.BrokerClient;
import pt.com.broker.client.HostInfo;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.broker.types.NetBrokerMessage;
import pt.com.broker.types.NetProtocolType;
import pt.com.gcs.messaging.GcsExecutor;

public class Producers
{

	private static final Logger log = LoggerFactory.getLogger(Producers.class);

	private final int producersCount;
	private final String destinatination;
	private final DestinationType destinationType;
	private final int messagesPerSecond;
	private final HostInfo hostInfo;
	private final NetProtocolType protocolType;
	private final String appName;

	private int messageSize = 512;

	private long messageExpiration = 0;

	private List<ProducerInfo> producers;

	private ScheduledThreadPoolExecutor shed_exec_srv;

	private NetBrokerMessage brokerMessage;

	volatile boolean stop = false;

	private final int messageBurst;

	private static List<Producers> activeProducers = new ArrayList<Producers>();

	static
	{
		final long interval = 15;

		Runnable command = new Runnable()
		{
			@Override
			public void run()
			{
				StringBuffer sb = new StringBuffer();

				for (Producers producers : activeProducers)
				{
					sb.append("\nProducers:");
					sb.append(producers.appName);
					sb.append("\n");
					for (ProducerInfo pi : producers.producers)
					{
						sb.append(String.format("Messages send in the last %s (s) by %s: %s\n", interval, pi.producerName, pi.messagesSend));
						pi.messagesSend = 0;
					}
				}

				System.out.println(sb.toString());
			}
		};

		GcsExecutor.scheduleAtFixedRate(command, interval, interval, TimeUnit.SECONDS);

	}

	private class ProducerInfo
	{
		public BrokerClient brokerClient;
		public String producerName;
		public volatile long messagesSend = 0;
	}

	public Producers(int producersCount, String destinatination, DestinationType destinationType, int messagesPerSecond, int messageBurst, HostInfo hostInfo, NetProtocolType protocolType, String appName)
	{
		this.producersCount = producersCount;
		this.destinatination = destinatination;
		this.destinationType = destinationType;
		this.messagesPerSecond = messagesPerSecond;
		this.messageBurst = messageBurst;
		this.hostInfo = hostInfo;
		this.protocolType = protocolType;
		this.appName = appName;

		if (messagesPerSecond >= 1000)
		{
			throw new IllegalArgumentException("messagesPerSecond >= 1000");
		}

		if ((messagesPerSecond != 0) && (messageBurst != 0))
		{
			throw new IllegalArgumentException("(messagesPerSecond != 0) && (messageBurst != 0)");
		}

		shed_exec_srv = CustomExecutors.newScheduledThreadPool(producersCount, "Producers-Sched");
	}

	public void init()
	{
		producers = new ArrayList<ProducerInfo>(producersCount);
		for (int i = 0; i != producersCount; ++i)
		{
			ProducerInfo pi = new ProducerInfo();
			try
			{
				pi.brokerClient = new BrokerClient(hostInfo.getHostname(), hostInfo.getPort(), String.format("Producer:%s:%s", destinatination, i), protocolType);

				pi.producerName = appName + i;

				producers.add(pi);
			}
			catch (Throwable e)
			{
				e.printStackTrace();
			}
		}
	}

	public void start()
	{
		String randomStr = RandomStringUtils.random(messageSize);
		brokerMessage = new NetBrokerMessage(randomStr);
		if (messageExpiration != 0)
		{
			brokerMessage.setExpiration(System.currentTimeMillis() + messageExpiration);
		}

		for (ProducerInfo pInfo : producers)
		{
			final ProducerInfo producerInfo = pInfo;

			Runnable nMessagesPerSecond = new Runnable()
			{
				@Override
				public void run()
				{
					while (true)
					{
						try
						{
							if (destinationType == DestinationType.TOPIC)
							{
								producerInfo.brokerClient.publishMessage(brokerMessage, destinatination);
							}
							else
							{
								producerInfo.brokerClient.enqueueMessage(brokerMessage, destinatination);
							}
							++producerInfo.messagesSend;
							if (stop)
								return;
							Sleep.time(1000 / messagesPerSecond);
						}
						catch (Throwable e)
						{
							log.error("Failed to publish message", e);
						}
					}
				}
			};

			Runnable messageBurstRunnable = new Runnable()
			{
				@Override
				public void run()
				{
					int count = messageBurst;
					while ((count--) != 0)
					{
						try
						{
							if (destinationType == DestinationType.TOPIC)
							{
								producerInfo.brokerClient.publishMessage(brokerMessage, destinatination);
							}
							else
							{
								producerInfo.brokerClient.enqueueMessage(brokerMessage, destinatination);
							}
							++producerInfo.messagesSend;
							if (stop)
								return;
						}
						catch (Throwable e)
						{
							log.error("Failed to publish message", e);
						}
					}
				}
			};

			if (messageBurst == 0)
			{
				shed_exec_srv.execute(nMessagesPerSecond);
			}
			else
			{
				shed_exec_srv.scheduleAtFixedRate(messageBurstRunnable, 5, 5, TimeUnit.SECONDS);

			}
		}
		activeProducers.add(this);
	}

	public void end()
	{
		stop = true;
		shed_exec_srv.shutdownNow();

		for (ProducerInfo pi : producers)
		{
			pi.brokerClient.close();
		}

		activeProducers.remove(this);
	}

	public void setMessageSize(int messageSize)
	{
		this.messageSize = messageSize;
	}

	public int getMessageSize()
	{
		return messageSize;
	}

	public void setMessageExpiration(long messageExpiration)
	{
		this.messageExpiration = messageExpiration;
	}

	public long getMessageExpiration()
	{
		return messageExpiration;
	}

}
