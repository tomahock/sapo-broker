package pt.com.broker.jsbridge.transformers;

import pt.com.broker.jsbridge.MessageTransformer;
import pt.com.broker.types.NetBrokerMessage;

public class ToUpperMessageTransformer implements MessageTransformer
{
	@Override
	public NetBrokerMessage transform(NetBrokerMessage message)
	{
		return new NetBrokerMessage(new String(message.getPayload()).toUpperCase().getBytes());
	}

}
