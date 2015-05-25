package pt.com.broker.client.nio.events;

import pt.com.broker.client.nio.server.HostInfo;
import pt.com.broker.types.NetMessage;

/**
 * Created by luissantos on 21-04-2014.
 *
 * @author vagrant
 * @version $Id: $Id
 */
public interface BrokerListener
{

	/**
	 * Fired when a message arrives.
	 *
	 * @param message
	 *            The message.
	 * @param host
	 *            a {@link pt.com.broker.client.nio.server.HostInfo} object.
	 * @throws java.lang.Throwable
	 *             if any.
	 */
	public void deliverMessage(NetMessage message, HostInfo host) throws Throwable;

}
