package unit.pt.com.broker.client.nio.ignore;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.com.broker.client.nio.SslBrokerClient;
import pt.com.broker.client.nio.events.PongListenerAdapter;
import pt.com.broker.types.NetPong;
import pt.com.broker.types.NetProtocolType;

import java.util.concurrent.Future;

/**
 * Created by luissantos on 05-05-2014.
 */
public class BrokerSslClientTest {

    private static final Logger log = LoggerFactory.getLogger(BrokerSslClientTest.class);


    @Test
    public void testPingPong() throws Throwable {

        SslBrokerClient bk = new SslBrokerClient("broker.wallet.pt", 3390, NetProtocolType.JSON);

        Future f = bk.connect();

        f.get();



        bk.checkStatus(new PongListenerAdapter() {
            @Override
            public void onMessage(NetPong message) {

                log.debug("Got pong message");

            }

        });


        Thread.sleep(10000);

    }
}
