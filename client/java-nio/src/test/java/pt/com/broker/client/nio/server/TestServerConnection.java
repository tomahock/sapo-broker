package pt.com.broker.client.nio.server;



import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Test;

import pt.com.broker.client.nio.bootstrap.Bootstrap;
import pt.com.broker.client.nio.bootstrap.ChannelInitializer;
import pt.com.broker.client.nio.codecs.BindingSerializerFactory;
import pt.com.broker.client.nio.consumer.PongConsumerManager;
import pt.com.broker.client.nio.mocks.SocketServer;
import pt.com.broker.client.nio.tests.Utils;
import pt.com.broker.types.BindingSerializer;
import pt.com.broker.types.NetProtocolType;
import io.netty.buffer.PooledByteBufAllocator;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by luissantos on 12-05-2014.
 */

//FIXME: Isn't this supposed to be an integration test?
public class TestServerConnection extends ServerBaseTest {

    @Test()
    public void testConnection() throws ExecutionException, InterruptedException, TimeoutException, IllegalAccessException, InstantiationException, ClassNotFoundException {
        List<SocketServer> servers = getServers();
        BindingSerializer  serializer = BindingSerializerFactory.getInstance(NetProtocolType.JSON);
        Bootstrap b = new Bootstrap(new ChannelInitializer(serializer,null,null, null), new PooledByteBufAllocator(true));
        HostContainer container = new HostContainer(b);
        for(SocketServer server : servers){
            HostInfo host = new HostInfo("127.0.0.1", server.getPort());
            host.setReaderIdleTime(4000);
            host.setWriterIdleTime(2000);
            container.add(host);
        }
        Future<HostInfo> future = container.connectAsync();
        HostInfo hostInfo = future.get(10000,TimeUnit.MILLISECONDS);
        Assert.assertNotNull(hostInfo);
        //container.shutdown();
        //f.get(10000,TimeUnit.MILLISECONDS);
        ShutDownServers(servers);
    }



    @Test()
    public void testClosedServers() throws InterruptedException, TimeoutException, ExecutionException, IllegalAccessException, InstantiationException, ClassNotFoundException {

        if(skipTest(Utils.isAndroid())){
            return;
        }

        List<SocketServer> servers = getServers();

        BindingSerializer  serializer = BindingSerializerFactory.getInstance(NetProtocolType.JSON);

        HostContainer container = new HostContainer(new Bootstrap(new ChannelInitializer( serializer,null,null, null), new PooledByteBufAllocator(true)));

        for(SocketServer server : servers){
            container.add(new HostInfo("127.0.0.1",server.getPort()));
        }


        int total_servers = container.size();

        Future<HostInfo> future = container.connectAsync();

        HostInfo host = future.get(20000L,TimeUnit.MILLISECONDS);

        Assert.assertNotNull(host);

        Assert.assertTrue(host.isActive());

        System.out.println("------------------------------------");

        Thread.sleep(4000);

        for(HostInfo chost : container.getConnectedHosts()){
            System.out.println("Status: "+chost.getStatus());
        }

        Collection<SocketServer> rservers  = getRandomServers(servers);

        System.out.println("Random servers: "+rservers.size());
        ShutDownServers(rservers);



        Thread.sleep(4000);

        int not_connected = container.notConnectedHosts().size();
        int connected_servers = container.getConnectedSize();


        for(HostInfo chost : container.getConnectedHosts()){
            System.out.println("Status: "+chost.getStatus());
        }

        System.out.println("Connected Servers: "+connected_servers);
        System.out.println("Not Connected Servers: "+not_connected);

        Assert.assertEquals(total_servers, (not_connected + connected_servers ));

        ShutDownServers(servers);


        container.disconnect().get();

        for(HostInfo chost : container.notConnectedHosts()){
            System.out.println("Status: "+chost.getStatus());
        }

        Thread.sleep(4000);


    }



    @Test()
    public void testHeartbeat() throws IOException, InterruptedException, TimeoutException, ExecutionException, IllegalAccessException, InstantiationException, ClassNotFoundException {

        if(skipTest(Utils.isAndroid())){
            return;
        }

        if(!userHasPermissions()){
            return;
        }

        List<SocketServer> servers = getServers();

        BindingSerializer  serializer = BindingSerializerFactory.getInstance(NetProtocolType.JSON);

        Bootstrap bootstrap = new Bootstrap(new ChannelInitializer(serializer,null,new PongConsumerManager(), null), new PooledByteBufAllocator(true));

        HostContainer container = new HostContainer(bootstrap);


        for(SocketServer server : servers){
            HostInfo hostInfo = new HostInfo("127.0.0.1",server.getPort());
            hostInfo.setConnectTimeout(500);
            hostInfo.setReaderIdleTime(4000);
            hostInfo.setWriterIdleTime(2000);

            container.add(hostInfo);
        }

        Future<HostInfo> future = container.connectAsync();

        HostInfo host = future.get(20000L,TimeUnit.MILLISECONDS);

        // Wait for a bit for the client to get connected with all the servers
        Thread.sleep(2000);


        Collection<SocketServer> random_servers = getRandomServers(servers);

        for(SocketServer s : random_servers){

            if(!ipTables.blockPort(currentChainName(),s.getPort())){
                System.out.println("Error blocking port");
            }

            System.out.println("Blocking server: "+s.getPort());

        }

        int blockedservers = random_servers.size();

        Thread.sleep(15000);

        int connected_servers = container.getConnectedHosts().size();


        System.out.println("Connected Servers: "+connected_servers);
        System.out.println("Blocked Servers: "+blockedservers);


        Assert.assertEquals(container.size(), connected_servers + blockedservers);

        ShutDownServers(servers);

    }



    @Test()
    public void testHeartbeatWithReconnect() throws IOException, InterruptedException, TimeoutException, ExecutionException, IllegalAccessException, InstantiationException, ClassNotFoundException {

        if(!userHasPermissions()){

            return;
        }

        List<SocketServer> servers = getServers();

        BindingSerializer  serializer = BindingSerializerFactory.getInstance(NetProtocolType.JSON);

        Bootstrap bootstrap = new Bootstrap(new ChannelInitializer(serializer,null,new PongConsumerManager(), null), new PooledByteBufAllocator(true));

        HostContainer container = new HostContainer(bootstrap);


        for(SocketServer server : servers){
            HostInfo host = new HostInfo("127.0.0.1",server.getPort());
            host.setConnectTimeout(500);
            host.setReaderIdleTime(4000);
            host.setWriterIdleTime(2000);

            container.add(host);
        }

        Future<HostInfo> future = container.connectAsync();

        HostInfo host = future.get(20000L,TimeUnit.MILLISECONDS);

        // Wait for a bit for the client to get connected with all the servers
        Thread.sleep(2000);


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
