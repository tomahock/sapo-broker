package pt.com.broker.functests.topicPubSub;

import pt.com.broker.client.BrokerClient;
import pt.com.broker.functests.Action;
import pt.com.broker.functests.Step;
import pt.com.broker.functests.Test;
import pt.com.types.NetPong;

public class PingTest extends Test
{

	public PingTest()
	{
		super("Ping pong test");
	}

	@Override
	protected void build() throws Throwable
	{
		setAction(new Action("Ping", "Client"){

			@Override
			public Step call() throws Exception
			{
				try
				{
					BrokerClient bk = new BrokerClient("127.0.0.1", 3323, "tcp://mycompany.com/mypublisher");
				
					NetPong pong = bk.checkStatus();

					bk.close();

					setDone(true);
					setSucess(true);
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
