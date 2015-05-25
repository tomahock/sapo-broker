package pt.com.broker.client.nio.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.client.nio.BrokerClient;
import pt.com.broker.client.nio.server.HostInfo;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetNotification;

/**
 * Created by luissantos on 26-05-2014.
 *
 * @author vagrant
 * @version $Id: $Id
 */
public abstract class NotificationListenerAdapter implements BrokerListener
{

	private static final Logger log = LoggerFactory.getLogger(NotificationListenerAdapter.class);

	BrokerClient brokerClient;

	/** {@inheritDoc} */
	@Override
	public final void deliverMessage(NetMessage message, HostInfo host) throws Throwable
	{
		NetAction netAction = message.getAction();
		NetNotification netNotification = netAction.getNotificationMessage();
		if (netNotification != null)
		{
			if (onMessage(netNotification, host))
			{
				if (brokerClient != null)
				{
					// acknowledge if not a topic
					if (netNotification.getDestinationType() != NetAction.DestinationType.TOPIC)
					{
						brokerClient.acknowledge(netNotification, host);
					}
				}
				else
				{
					log.debug("Message not acknowledged because broker client was not set ");
				}
			}
		}
		else
		{
			log.error("Invalid Message");
		}
	}

	/**
	 * <p>
	 * Setter for the field <code>brokerClient</code>.
	 * </p>
	 *
	 * @param client
	 *            a {@link pt.com.broker.client.nio.BrokerClient} object.
	 */
	public final void setBrokerClient(BrokerClient client)
	{
		brokerClient = client;
	}

	/**
	 * <p>
	 * onMessage.
	 * </p>
	 *
	 * @param notification
	 *            a {@link pt.com.broker.types.NetNotification} object.
	 * @param host
	 *            a {@link pt.com.broker.client.nio.server.HostInfo} object.
	 * @return a boolean.
	 */
	public abstract boolean onMessage(NetNotification notification, HostInfo host);

}
