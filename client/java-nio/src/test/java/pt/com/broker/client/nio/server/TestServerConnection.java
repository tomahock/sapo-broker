package pt.com.broker.client.nio.server;


import junit.framework.Assert;
import pt.com.broker.client.nio.bootstrap.Bootstrap;
import pt.com.broker.client.nio.bootstrap.ChannelInitializer;
import pt.com.broker.client.nio.codecs.BindingSerializerFactory;
import pt.com.broker.client.nio.consumer.PongConsumerManager;
import pt.com.broker.client.nio.mocks.SocketServer;
import pt.com.broker.client.nio.tests.Utils;
import pt.com.broker.types.BindingSerializer;
import pt.com.broker.types.NetProtocolType;

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
public class TestServerConnection extends ServerBaseTest {






    public void testConnection() throws ExecutionException, InterruptedException, TimeoutException, IllegalAccessException, InstantiationException, ClassNotFoundException {


        List<SocketServer> servers = getServers();

        BindingSerializer  serializer = BindingSerializerFactory.getInstance(NetProtocolType.JSON);


        Bootstrap b = new Bootstrap(new ChannelInitializer(serializer,null,null));

        HostContainer container = new HostContainer(b);


        for(SocketServer server : servers){
            container.add(new HostInfo("127.0.0.1",server.getPort()));
        }

        Future<HostInfo> future = container.connectAsync();



        HostInfo hostInfo = future.get(10000,TimeUnit.MILLISECONDS);


        Assert.assertNotNull(hostInfo);

        //container.shutdown();

        //f.get(10000,TimeUnit.MILLISECONDS);

        ShutDownServers(servers);

    }



    public void testClosedServers() throws InterruptedException, TimeoutException, ExecutionException, IllegalAccessException, InstantiationException, ClassNotFoundException {

        if(skipTest(Utils.isAndroid())){
            return;
        }

        List<SocketServer> servers = getServers();

        BindingSerializer  serializer = BindingSerializerFactory.getInstance(NetProtocolType.JSON);

        HostContainer container = new HostContainer(new Bootstrap(new ChannelInitializer( serializer,null,null)));

        for(SocketServer server : servers){
            container.add(new HostInfo("127.0.0.1",server.getPort()));
        }


        int total_servers = container.size();

        Future<HostInfo> future = container.connectAsync();

        HostInfo host = future.get(20000L,TimeUnit.MILLISECONDS);

        Assert.assertNotNull(host);

        Assert.assertTrue(host.isActive());

        System.out.println("------------------------------------");



        ShutDownServers(getRandomServers(servers));



        Thread.sleep(2000);

        int not_connected = container.notConnectedHosts().size();
        int connected_servers = container.getConnectedSize();


        Assert.assertEquals(total_servers, (not_connected + connected_servers ));

        ShutDownServers(servers);

    }



    public void testHeartbeat() throws IOException, InterruptedException, TimeoutException, ExecutionException, IllegalAccessException, InstantiationException, ClassNotFoundException {

        if(skipTest(Utils.isAndroid())){
            return;
        }

        if(!userHasPermissions()){
            return;
        }

        List<SocketServer> servers = getServers();

        BindingSerializer  serializer = BindingSerializerFactory.getInstance(NetProtocolType.JSON);

        Bootstrap bootstrap = new Bootstrap(new ChannelInitializer(serializer,null,new PongConsumerManager()));

        HostContainer container = new HostContainer(bootstrap);


        for(SocketServer server : servers){
            HostInfo hostInfo = new HostInfo("127.0.0.1",server.getPort());
            hostInfo.setConnectTimeout(500);
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



    public void testHeartbeatWithReconnect() throws IOException, InterruptedException, TimeoutException, ExecutionException, IllegalAccessException, InstantiationException, ClassNotFoundException {

        if(!userHasPermissions()){

            return;
        }

        List<SocketServer> servers = getServers();

        BindingSerializer  serializer = BindingSerializerFactory.getInstance(NetProtocolType.JSON);

        Bootstrap bootstrap = new Bootstrap(new ChannelInitializer(serializer,null,new PongConsumerManager()));

        HostContainer container = new HostContainer(bootstrap);


        for(SocketServer server : servers){
            HostInfo host = new HostInfo("127.0.0.1",server.getPort());
            host.setConnectTimeout(500);
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
