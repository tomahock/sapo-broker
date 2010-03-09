package pt.com.broker.jsbridge;

import pt.com.broker.types.NetBrokerMessage;

public interface MessageTransformer
{
	NetBrokerMessage transform(NetBrokerMessage message);
}
