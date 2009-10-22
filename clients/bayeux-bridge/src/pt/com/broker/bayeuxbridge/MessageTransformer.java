package pt.com.broker.bayeuxbridge;

import pt.com.broker.types.NetBrokerMessage;

public interface MessageTransformer
{
	NetBrokerMessage transform(NetBrokerMessage message);
	void init();
}
