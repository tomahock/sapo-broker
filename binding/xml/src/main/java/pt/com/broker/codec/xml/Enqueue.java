package pt.com.broker.codec.xml;

public class Enqueue
{
	public String actionId;

	public BrokerMessage brokerMessage;

	public Enqueue()
	{
		brokerMessage = new BrokerMessage();
	}
}
