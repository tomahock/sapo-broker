package pt.com.broker.client.nio.events;

import pt.com.broker.client.nio.server.HostInfo;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetPong;

/**
 * Created by luissantos on 30-04-2014.
 *
 * @author vagrant
 * @version $Id: $Id
 */
public abstract class PongListenerAdapter implements BrokerListener
{

	/** {@inheritDoc} */
	@Override
	public final void deliverMessage(NetMessage message, HostInfo host) throws Throwable
	{

		NetPong netPong = message.getAction().getPongMessage();

		if (netPong == null)
		{
			return;
		}

		this.onMessage(netPong, host);

	}

	/**
	 * <p>
	 * onMessage.
	 * </p>
	 *
	 * @param message
	 *            a {@link pt.com.broker.types.NetPong} object.
	 * @param hostInfo
	 *            a {@link pt.com.broker.client.nio.server.HostInfo} object.
	 */
	public abstract void onMessage(NetPong message, HostInfo hostInfo);

}
