import org.caudexorigo.text.RandomStringUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.com.broker.client.nio.UdpBrokerClient;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetProtocolType;

import java.util.concurrent.Future;

/**
 * Created by luissantos on 05-05-2014.
 */
public class UdpBrokerClientTest {

    private static final Logger log = LoggerFactory.getLogger(UdpBrokerClientTest.class);


    @Test
    public void testSendPacket() throws Exception{

        UdpBrokerClient bk = new UdpBrokerClient("192.168.100.1", 3323, NetProtocolType.JSON);


        Future f = bk.connect();

        f.get();

        bk.publishMessage(RandomStringUtils.randomAlphanumeric(10), "/teste/", NetAction.DestinationType.QUEUE);



        Thread.sleep(2000);
    }


    @Test
    public void testSendPacketOldFrame() throws Exception{

        UdpBrokerClient bk = new UdpBrokerClient("192.168.100.1", 3366, NetProtocolType.SOAP_v0);


        Future f = bk.connect();

        f.get();

        bk.publishMessage(RandomStringUtils.randomAlphanumeric(10), "/teste/", NetAction.DestinationType.QUEUE);



        Thread.sleep(2000);
    }
}
