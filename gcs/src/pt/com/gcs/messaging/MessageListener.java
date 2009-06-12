package pt.com.gcs.messaging;

import pt.com.broker.types.NetAction.DestinationType;

/**
 * Classes implement MessageListener in order to be notified of new topic publications. 
 *
 */

public interface MessageListener
{
	public boolean onMessage(InternalMessage message);

	public String getDestinationName();

	public DestinationType getDestinationType();

}
