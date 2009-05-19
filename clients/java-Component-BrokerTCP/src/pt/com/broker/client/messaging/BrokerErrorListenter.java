package pt.com.broker.client.messaging;

import pt.com.broker.types.NetFault;

public interface BrokerErrorListenter
{
	void onFault(NetFault fault);

	void onError(Throwable throwable);

}
