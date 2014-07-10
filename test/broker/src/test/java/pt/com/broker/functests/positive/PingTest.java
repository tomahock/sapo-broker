package pt.com.broker.functests.positive;



import pt.com.broker.functests.Action;
import pt.com.broker.client.nio.BrokerClient;
import pt.com.broker.client.nio.server.HostInfo;
import pt.com.broker.client.nio.events.PongListenerAdapter;
import pt.com.broker.functests.Step;
import pt.com.broker.functests.conf.ConfigurationInfo;
import pt.com.broker.functests.helpers.BrokerTest;
import pt.com.broker.types.NetPong;
import pt.com.broker.types.NetProtocolType;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class PingTest extends BrokerTest
{
    private final CountDownLatch latch = new CountDownLatch(1);


    public PingTest(NetProtocolType protocolType) {
        super(protocolType);
        setName("Ping pong test");
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


                    BrokerClient bk = new BrokerClient(getAgent1Hostname(), getAgent1Port(),getEncodingProtocolType());

                    bk.connect();




					Future f = bk.checkStatus(new PongListenerAdapter() {
                        @Override
                        public void onMessage(NetPong message, HostInfo host) {

                            setSucess(message != null);

                            latch.countDown();


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
