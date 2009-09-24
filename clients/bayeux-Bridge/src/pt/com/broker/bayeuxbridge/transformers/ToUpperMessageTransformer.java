package pt.com.broker.bayeuxbridge.transformers;

import pt.com.broker.bayeuxbridge.MessageTransformer;
import pt.com.broker.types.NetBrokerMessage;

public class ToUpperMessageTransformer implements MessageTransformer
{

	@Override
	public void init()
	{
	}

	@Override
	public NetBrokerMessage transform(NetBrokerMessage message)
	{
		return new NetBrokerMessage(new String(message.getPayload()).toUpperCase().getBytes());
	}

}
