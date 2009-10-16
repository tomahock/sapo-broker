package pt.com.broker.monitorization.collectors;

import pt.com.broker.client.BaseBrokerClient;
import pt.com.broker.types.NetNotification;

public class FaultsCollector extends Collector<FaultListener>
{
	private static final String SUBSCRIPTION = "/system/faults/.*";

	public FaultsCollector(BaseBrokerClient agent) throws Throwable
	{
		super("Faults Collector", SUBSCRIPTION, agent);
	}

	@Override
	protected void messageReceived(NetNotification notification)
	{
		String destination = notification.getDestination();

		String agent = destination.substring("/system/faults/#".length(), destination.length() - 1);

		String message = new String(notification.getMessage().getPayload());

		synchronized (listeners)
		{
			for (FaultListener handler : listeners)
			{
				try
				{
					handler.onFault(agent, message);
				}
				catch (Exception e)
				{
				}
			}
		}

	}

}
