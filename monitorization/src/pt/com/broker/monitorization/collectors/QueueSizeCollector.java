package pt.com.broker.monitorization.collectors;

import java.util.regex.Pattern;

import pt.com.broker.client.BaseBrokerClient;
import pt.com.broker.types.NetNotification;

/**
 * QueueSizeCollector collects information about the size of each queue (number of stored messages).
 * 
 */
public class QueueSizeCollector extends Collector<QueueSizeListener>
{
	// Destination sample: /system/stats/queue-size/#/queue/foo2#
	// Payload sample: 127.0.0.1#/queue/foo#1

	private static final String agentNameSizeRegEx = "#";
	Pattern agentNameSizePattern;

	private static final String SUBSCRIPTION = "/system/stats/queue-size/.*"; // TODO: optimize this

	public QueueSizeCollector(BaseBrokerClient agent) throws Throwable
	{
		super("Queue Size Collector", SUBSCRIPTION, agent);
		agentNameSizePattern = Pattern.compile(agentNameSizeRegEx);
	}

//	@Override
//	public void start() throws Throwable
//	{
//		BaseBrokerClient bc = getBrokerClient();
//
//		NetSubscribe netSub = new NetSubscribe(SUBSCRIPTION, DestinationType.TOPIC);
//		bc.addAsyncConsumer(netSub, new BrokerListener()
//		{
//
//			@Override
//			public boolean isAutoAck()
//			{
//				return false;
//			}
//
//			@Override
//			public void onMessage(NetNotification message)
//			{
//				messageReceived(message);
//			}
//
//		});
//	}

	@Override
	protected void messageReceived(NetNotification notification)
	{
		String agent = null;
		String queueName = null;
		int size = 0;

		String[] tokens = agentNameSizePattern.split(new String(notification.getMessage().getPayload()));

		agent = tokens[0];
		queueName = tokens[1];
		size = Integer.parseInt(tokens[2]);

		synchronized (listeners)
		{
			for (QueueSizeListener handler : listeners)
			{
				try
				{
					handler.onUpdate(agent, queueName, size);
				}
				catch (Exception e)
				{
					// TODO: log exception
				}
			}
		}
	}

}
