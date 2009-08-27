package pt.com.broker.codec.xml;

public class Publish
{
	public String actionId;

	public BrokerMessage brokerMessage;

	public Publish()
	{
		brokerMessage = new BrokerMessage();
	}
}
