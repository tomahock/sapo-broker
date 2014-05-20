package pt.com.broker.functests.positive;


import io.netty.channel.ChannelFuture;
import pt.com.broker.client.nio.BrokerClient;
import pt.com.broker.client.nio.events.BrokerListenerAdapter;
import pt.com.broker.functests.Action;
import pt.com.broker.functests.Step;
import pt.com.broker.functests.conf.ConfigurationInfo;
import pt.com.broker.functests.helpers.BrokerTest;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetPong;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class PingTest extends BrokerTest
{
    private final CountDownLatch latch = new CountDownLatch(1);

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


                    BrokerClient bk = new BrokerClient(ConfigurationInfo.getParameter("agent1-host"), BrokerTest.getAgent1Port(),getEncodingProtocolType());

                    bk.connect().get();




					ChannelFuture f = bk.checkStatus(new BrokerListenerAdapter() {
                        @Override
                        public boolean onMessage(NetMessage message) {

                            NetPong  pong = message.getAction().getPongMessage();

                            System.out.println("Pong: " + pong.getActionId());

                            setSucess(pong != null);

                            latch.countDown();

                            return true;

                        }


                    });


                    latch.await(2000, TimeUnit.MILLISECONDS);

                    setDone(true);
					bk.close();




				}
				catch (Throwable t)
				{
                    t.printStackTrace();
					setFailure(t);
				}
				return this;
			}

		});
	}

}
