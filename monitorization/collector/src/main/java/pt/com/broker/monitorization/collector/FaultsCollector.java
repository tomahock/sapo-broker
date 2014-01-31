package pt.com.broker.monitorization.collector;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.client.BaseBrokerClient;
import pt.com.broker.client.messaging.BrokerListener;
import pt.com.broker.monitorization.db.FaultsDB;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.broker.types.NetBrokerMessage;
import pt.com.broker.types.NetNotification;
import pt.com.broker.types.NetSubscribe;

public class FaultsCollector
{
	private static Logger log = LoggerFactory.getLogger(FaultsCollector.class);

	private final String SUBSCRIPTION = "/system/faults/.*";
	private final BaseBrokerClient brokerClient;

	public FaultsCollector(BaseBrokerClient brokerClient)
	{
		this.brokerClient = brokerClient;
	}

	public void start()
	{
		BaseBrokerClient bc = getBrokerClient();

		NetSubscribe netSub = new NetSubscribe(getSubscription(), DestinationType.TOPIC);
		try
		{
			bc.addAsyncConsumer(netSub, new BrokerListener()
			{
				@Override
				public boolean isAutoAck()
				{
					return false;
				}

				@Override
				public void onMessage(NetNotification notification)
				{
					messageReceived(notification);
				}
			});
		}
		catch (Throwable t)
		{
			log.error("Failed to initilize subscription.", t);
		}
	}

	private void messageReceived(NetNotification notification)
	{
		String content = new String(notification.getMessage().getPayload());
		try
		{
			String destination = notification.getDestination();
			String agent = destination.substring("/system/faults/#".length(), destination.length() - 1);
			FaultsDB.add(agent, new Date(), content);

		}
		catch (Throwable t)
		{
			log.error(String.format("Failed to process received message. Error: %s. Message: \n'%s'", t.getMessage(), content));
		}
	}

	public BaseBrokerClient getBrokerClient()
	{
		return brokerClient;
	}

	public String getSubscription()
	{
		return SUBSCRIPTION;
	}

	public static void main(String[] args)
	{
		try
		{
			String xml = "<stats date='2010-04-06T15:09:25.650Z' agent-name='127.0.0.1:3315'><item subject='queue:///queue/foo' predicate='input-rate' value='123' /><item subject='queue:///queue/foo' predicate='output-rate' value='23' /> 	<item subject='queue:///queue/foo' predicate='subscriptions' value='11223' /> 	<item subject='queue:///queue/foo' predicate='failed' value='123213' /> 	<item subject='queue:///queue/foo' predicate='expired' value='3' /> 	<item subject='queue:///queue/foo' predicate='redelivered' value='23' /> </stats>";

			NetNotification notification = new NetNotification("/system/stats/....", DestinationType.TOPIC, new NetBrokerMessage(xml), "/system/stats/.*");

			new FaultsCollector(null).messageReceived(notification);

			System.out.println("END");
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}
	}
}
