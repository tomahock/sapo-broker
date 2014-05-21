package pt.com.broker.functests.helpers;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


import pt.com.broker.client.nio.events.BrokerListener;
import pt.com.broker.client.nio.events.BrokerListenerAdapter;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetNotification;

public class MultipleNotificationsBrokerListener extends BrokerListenerAdapter
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
	public boolean onMessage(NetMessage message)
	{

        NetNotification netNotification = message.getAction().getNotificationMessage();

		synchronized (list)
		{
			list.add( netNotification);
			if (list.size() == expectedNotifications)
			{
				value.set(list);
			}
		}

        return true;
	}

	public SetValueFuture<List<NetNotification>> getFuture()
	{
		return value;
	}

}
