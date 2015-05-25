package pt.com.broker.client.nio.events;

import pt.com.broker.client.nio.server.HostInfo;
import pt.com.broker.types.NetFault;
import pt.com.broker.types.NetMessage;

/**
 * Created by luissantos on 30-05-2014.
 *
 * @author vagrant
 * @version $Id: $Id
 */
public abstract class ErrorListenerAdapter implements BrokerListener
{

	/** {@inheritDoc} */
	@Override
	public final void deliverMessage(NetMessage message, HostInfo host) throws Throwable
	{

		NetFault netFault = message.getAction().getFaultMessage();

		if (netFault != null)
		{
			onMessage(netFault, host);
		}

	}

	/**
	 * <p>
	 * onMessage.
	 * </p>
	 *
	 * @param message
	 *            a {@link pt.com.broker.types.NetFault} object.
	 * @param hostInfo
	 *            a {@link pt.com.broker.client.nio.server.HostInfo} object.
	 */
	public abstract void onMessage(NetFault message, HostInfo hostInfo);

}
