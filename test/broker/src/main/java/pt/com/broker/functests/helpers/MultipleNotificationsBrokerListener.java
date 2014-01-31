package pt.com.broker.functests.helpers;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import pt.com.broker.client.messaging.BrokerListener;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.broker.types.NetNotification;

public class MultipleNotificationsBrokerListener implements BrokerListener
{

	private NetAction.DestinationType destinationType;
	private List<NetNotification> list;
	private int expectedNotifications;
	private SetValueFuture<List<NetNotification>> value = new SetValueFuture<List<NetNotification>>();

	public MultipleNotificationsBrokerListener(NetAction.DestinationType destinationType, int expectedNotifications)
	{
		this.destinationType = destinationType;
		this.expectedNotifications = expectedNotifications;
		// this.list = new ArrayList<NetNotification>(expectedNotifications);
		this.list = new CopyOnWriteArrayList<NetNotification>();
	}

	@Override
	public boolean isAutoAck()
	{
		return destinationType != DestinationType.TOPIC;
	}

	@Override
	public void onMessage(NetNotification message)
	{
		synchronized (list)
		{
			list.add(message);
			if (list.size() == expectedNotifications)
			{
				value.set(list);
			}
		}

	}

	public SetValueFuture<List<NetNotification>> getFuture()
	{
		return value;
	}

}
