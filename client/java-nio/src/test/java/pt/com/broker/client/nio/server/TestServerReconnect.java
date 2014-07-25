package pt.com.broker.client.nio.server;


import org.junit.Assert;
import org.junit.Test;
import pt.com.broker.client.nio.BrokerClient;
import pt.com.broker.client.nio.events.NotificationListenerAdapter;
import pt.com.broker.client.nio.mocks.SocketServer;
import pt.com.broker.client.nio.tests.Utils;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetNotification;
import pt.com.broker.types.NetProtocolType;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * Created by luissantos on 22-05-2014.
 */
public class TestServerReconnect extends ServerBaseTest {




    @Test()
    public void testHeartbeatWithReconnect() throws IOException, InterruptedException, TimeoutException, ExecutionException, IllegalAccessException, InstantiationException, ClassNotFoundException {

        if(skipTest(Utils.isAndroid())){
            return;
        }

        if(skipTest(!userHasPermissions())){
            return;
        }

        List<SocketServer> servers = getServers();

        BrokerClient bk = new BrokerClient(NetProtocolType.JSON);

        HostContainer container = bk.getHosts();

        for(SocketServer server : servers){
            HostInfo host = new HostInfo("127.0.0.1",server.getPort());
            host.setConnectTimeout(2000);

            host.setReaderIdleTime(4000);
            host.setWriterIdleTime(2000);

            bk.addServer(host);
        }

        bk.connect();


        // Wait for a bit for the client to get connected with all the servers
        Thread.sleep(2000);



        String name = "/teste/";

        System.out.println(name);
        bk.subscribe( name , NetAction.DestinationType.QUEUE , new NotificationListenerAdapter() {
            @Override
            public boolean onMessage(NetNotification message, HostInfo host) {

                return true;

            }
        });



        Collection<SocketServer> random_servers = getRandomServers(servers);

        for(SocketServer s : random_servers){

            if(!ipTables.blockPort(currentChainName(),s.getPort())){
                System.out.println("Error blocking port");
            }

            System.out.println("Blocking server: "+s.getPort());

        }

        System.out.println("------ sleeping-------");

        Thread.sleep(10000);

        int blockedservers = random_servers.size();

        int connected_servers = container.getConnectedSize();


        System.out.println("Connected Servers: "+connected_servers);
        System.out.println("Blocked Servers: "+blockedservers);

        Assert.assertEquals(container.getHostsSize(), connected_servers + blockedservers);


        for(SocketServer s : random_servers){

            if(!ipTables.removePortBlock(currentChainName(), s.getPort())){
                System.out.println("Error ublocking port");
            }

            System.out.println("Unblocking server: "+s.getPort());

        }



        Thread.sleep(10000);

        connected_servers = container.getConnectedSize();


        System.out.println("Total Servers: "+servers.size());
        System.out.println("Connected Servers: "+connected_servers);


        Assert.assertEquals(container.getHostsSize(), connected_servers);

        ShutDownServers(servers);
    }

}
