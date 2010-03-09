package pt.com.broker.jsbridge;

import pt.com.broker.types.NetBrokerMessage;

public class DefaultTransformer implements MessageTransformer
{
	@Override
	public NetBrokerMessage transform(NetBrokerMessage message)
	{
		return message;
	}
}
