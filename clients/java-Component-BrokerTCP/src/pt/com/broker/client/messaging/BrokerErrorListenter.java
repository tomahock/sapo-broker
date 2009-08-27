package pt.com.broker.client.messaging;

import pt.com.broker.types.NetFault;

/**
 * BrokerErrorListenter interface should be implemented by those who wish to be notified of errors that occur in consequence of message processing, locally or remotely.
 * 
 */

public interface BrokerErrorListenter
{
	void onFault(NetFault fault);

	void onError(Throwable throwable);

}
