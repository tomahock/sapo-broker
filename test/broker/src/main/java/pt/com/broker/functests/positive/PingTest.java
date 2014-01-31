package pt.com.broker.functests.positive;

import pt.com.broker.client.BrokerClient;
import pt.com.broker.functests.Action;
import pt.com.broker.functests.Step;
import pt.com.broker.functests.conf.ConfigurationInfo;
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
					BrokerClient bk = new BrokerClient(ConfigurationInfo.getParameter("agent1-host"), BrokerTest.getAgent1Port(), "tcp://mycompany.com/test", getEncodingProtocolType());

					NetPong pong = bk.checkStatus();

					System.out.println("Pong: " + pong.getActionId());

					bk.close();

					setDone(true);
					setSucess(pong != null);
				}
				catch (Throwable t)
				{
					setFailure(t);
				}
				return this;
			}

		});
	}

}
