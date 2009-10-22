package pt.com.broker.bayeuxbridge;

import pt.com.broker.types.NetBrokerMessage;

public class DefaultTransformer implements MessageTransformer
{
	@Override
	public NetBrokerMessage transform(NetBrokerMessage message)
	{
		return message;
	}
	
	private static DefaultTransformer instance = new DefaultTransformer();
	
	public static DefaultTransformer getInstance() { return instance;}

	@Override
	public void init()
	{
		
	}
		
}
