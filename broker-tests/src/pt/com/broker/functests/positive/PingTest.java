package pt.com.broker.functests.positive;

import pt.com.broker.client.BrokerClient;
import pt.com.broker.functests.Action;
import pt.com.broker.functests.Step;
import pt.com.broker.functests.helpers.BrokerTest;
import pt.com.broker.types.NetPong;

public class PingTest extends BrokerTest
{

	public PingTest()
	{
		super("Ping pong test");
	}

	@Override
	protected void build() throws Throwable
	{
		setAction(new Action("Ping", "Client")
		{

			@Override
			public Step run() throws Exception
			{
				try
				{
					BrokerClient bk = new BrokerClient("127.0.0.1", 3323, "tcp://mycompany.com/mypublisher", getEncodingProtocolType());

					NetPong pong = bk.checkStatus();

					bk.close();

					setDone(true);
					setSucess(pong != null);
				}
				catch (Throwable t)
				{
					throw new Exception(t);
				}
				return this;
			}

		});
	}

}
