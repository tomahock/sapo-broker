package pt.com.broker.client.nio;

import org.junit.Assert;
import org.junit.Test;
import pt.com.broker.client.nio.bootstrap.Bootstrap;
import pt.com.broker.client.nio.bootstrap.ChannelInitializer;
import pt.com.broker.client.nio.mocks.ServerFactory;
import pt.com.broker.client.nio.mocks.SocketServer;
import pt.com.broker.client.nio.server.HostContainer;
import pt.com.broker.types.NetProtocolType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by luissantos on 12-05-2014.
 */
public class TestServerConnection {




    protected List<SocketServer> getServers() {

        List<SocketServer> servers = new ArrayList<SocketServer>();

        int count = 1;

        while (count-- > 0){

            SocketServer server = ServerFactory.getInstance(3323);

            servers.add(server);

        }


        return servers;
    }

    @Test()
    public void testConnection() throws ExecutionException, InterruptedException, TimeoutException {


        List<SocketServer> servers = getServers();


        HostContainer container = new HostContainer(new Bootstrap(new ChannelInitializer(NetProtocolType.JSON,null,null)));


        for(SocketServer server : servers){
            container.add(new HostInfo("192.168.100.1",server.getPort()));
        }

        Future<HostInfo> future = container.connect();


        HostInfo hostInfo = future.get(10000,TimeUnit.MILLISECONDS);


        Assert.assertNotNull(hostInfo);


    }


    @Test()
    public void testClosedServers() throws InterruptedException, TimeoutException, ExecutionException {

        System.out.println("------------------------------------");

        List<SocketServer> servers = getServers();


        HostContainer container = new HostContainer(new Bootstrap(new ChannelInitializer(NetProtocolType.JSON,null,null)));


        for(SocketServer server : servers){
            container.add(new HostInfo("192.168.100.1",server.getPort()));
        }

        Future<HostInfo> future = container.connect();

        future.get(10000L,TimeUnit.MILLISECONDS);

        int connected_servers = container.notConnectedHosts().size();

        SocketServer s =  servers.get(0);

        Future f = s.shutdown();


        f.get(5000,TimeUnit.MILLISECONDS);


        Assert.assertEquals(connected_servers+1,container.notConnectedHosts().size());

    }
}
