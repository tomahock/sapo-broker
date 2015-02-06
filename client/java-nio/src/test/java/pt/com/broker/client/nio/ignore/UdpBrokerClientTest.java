package pt.com.broker.client.nio.ignore;

import org.apache.commons.lang3.RandomStringUtils;
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

    private static final String host = "192.168.100.1";

//    @Test
    public void testSendPacket() throws Exception{

        UdpBrokerClient bk = new UdpBrokerClient(host, 3323, NetProtocolType.JSON);


        bk.publish(RandomStringUtils.randomAlphanumeric(10), "/teste/", NetAction.DestinationType.QUEUE);



        Thread.sleep(2000);
    }


//    @Test
    public void testSendPacketOldFrame() throws Exception{

        UdpBrokerClient bk = new UdpBrokerClient(host, 3366, NetProtocolType.SOAP_v0);


        bk.publish(RandomStringUtils.randomAlphanumeric(10), "/teste/", NetAction.DestinationType.QUEUE);



        Thread.sleep(2000);
    }
}
