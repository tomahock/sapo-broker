import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.junit.Test;
import pt.com.broker.client.nio.BrokerClient;

import java.util.Timer;

/**
 * Created by luissantos on 21-04-2014.
 */
public class BrokerClientTest {


    @Test
    public void testClientConnect() throws Exception{

        BrokerClient bk = new BrokerClient("localhost",3323);

        ChannelFuture f = bk.connect();




        f.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                System.out.println("Connected");
            }
        });





        f.sync();



    }

}
