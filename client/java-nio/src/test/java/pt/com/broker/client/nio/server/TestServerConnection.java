package pt.com.broker.client.nio.server;

import org.junit.*;
import pt.com.broker.client.nio.HostInfo;
import pt.com.broker.client.nio.bootstrap.Bootstrap;
import pt.com.broker.client.nio.bootstrap.ChannelInitializer;
import pt.com.broker.client.nio.codecs.BindingSerializerFactory;
import pt.com.broker.client.nio.consumer.PongConsumerManager;
import pt.com.broker.client.nio.iptables.IpTables;
import pt.com.broker.client.nio.mocks.ServerFactory;
import pt.com.broker.client.nio.mocks.SocketServer;
import pt.com.broker.client.nio.server.HostContainer;
import pt.com.broker.types.BindingSerializer;
import pt.com.broker.types.NetProtocolType;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by luissantos on 12-05-2014.
 */
public class TestServerConnection {

    int totalServers = 50;

    static  IpTables ipTables = new IpTables();

    static  String chainName = "java-nio-tests";


    @BeforeClass()
    public static void setup() throws IOException, InterruptedException {



        if(ipTables.hasPermission()){


            if(!ipTables.addChain(currentChainName())){
                System.out.println("Error adding chain:"+currentChainName());
            }else{
                ipTables.addChaintoChain("OUTPUT",currentChainName());
            }

        }

    }

    public static String currentChainName(){

        String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];

        return chainName+"-"+pid;
    }

    @AfterClass()
    public static void cleanup() throws IOException, InterruptedException {

        if(ipTables.hasPermission()) {

            ipTables.removeChainfromChain("OUTPUT",currentChainName());

            ipTables.deleteChain(currentChainName());

        }


    }



    protected List<SocketServer> getServers() {

        List<SocketServer> servers = new ArrayList<SocketServer>();

        int count = (int) (1 + (Math.random() *  totalServers));

        while (count-- > 0){

            SocketServer server = ServerFactory.getInstance(0);

            servers.add(server);

        }


        return servers;
    }

    @Test()
    public void testConnection() throws ExecutionException, InterruptedException, TimeoutException, IllegalAccessException, InstantiationException, ClassNotFoundException {


        List<SocketServer> servers = getServers();

        BindingSerializer  serializer = BindingSerializerFactory.getInstance(NetProtocolType.JSON);


        Bootstrap b = new Bootstrap(new ChannelInitializer(serializer,null,null));

        HostContainer container = new HostContainer(b);


        for(SocketServer server : servers){
            container.add(new HostInfo("127.0.0.1",server.getPort()));
        }

        Future<HostInfo> future = container.connect();


        HostInfo hostInfo = future.get(10000,TimeUnit.MILLISECONDS);


        Assert.assertNotNull(hostInfo);

        //container.shutdown();

        //f.get(10000,TimeUnit.MILLISECONDS);

        ShutDownServers(servers);

    }


    @Test()
    public void testClosedServers() throws InterruptedException, TimeoutException, ExecutionException, IllegalAccessException, InstantiationException, ClassNotFoundException {

        List<SocketServer> servers = getServers();

        BindingSerializer  serializer = BindingSerializerFactory.getInstance(NetProtocolType.JSON);

        HostContainer container = new HostContainer(new Bootstrap(new ChannelInitializer( serializer,null,null)));

        for(SocketServer server : servers){
            container.add(new HostInfo("127.0.0.1",server.getPort()));
        }


        int total_servers = container.size();

        Future<HostInfo> future = container.connect();

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


    @Test()
    public void testHeartbeat() throws IOException, InterruptedException, TimeoutException, ExecutionException, IllegalAccessException, InstantiationException, ClassNotFoundException {

        Assume.assumeTrue(userHasPermissions());

        List<SocketServer> servers = getServers();

        BindingSerializer  serializer = BindingSerializerFactory.getInstance(NetProtocolType.JSON);

        Bootstrap bootstrap = new Bootstrap(new ChannelInitializer(serializer,null,new PongConsumerManager()));

        HostContainer container = new HostContainer(bootstrap);


        for(SocketServer server : servers){
            HostInfo hostInfo = new HostInfo("127.0.0.1",server.getPort());
            hostInfo.setConnectTimeout(500);
            container.add(hostInfo);
        }

        Future<HostInfo> future = container.connect();

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

        Assume.assumeTrue(userHasPermissions());

        List<SocketServer> servers = getServers();

        BindingSerializer  serializer = BindingSerializerFactory.getInstance(NetProtocolType.JSON);

        Bootstrap bootstrap = new Bootstrap(new ChannelInitializer(serializer,null,new PongConsumerManager()));

        HostContainer container = new HostContainer(bootstrap);


        for(SocketServer server : servers){
            HostInfo host = new HostInfo("127.0.0.1",server.getPort());
            host.setConnectTimeout(500);
            container.add(host);
        }

        Future<HostInfo> future = container.connect();

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




    protected List<SocketServer> getRandomServers(Collection<SocketServer> servers){


        List<SocketServer> _servers = new ArrayList<>(servers);

        Collections.shuffle(_servers);

        return _servers.subList(0, (int) (Math.random() *  _servers.size()));
    }


    public boolean userHasPermissions(){

        IpTables ipTables = new IpTables();


        return ipTables.hasPermission();

    }

    protected void ShutDownServers(Collection<SocketServer> servers){

        for(SocketServer s : servers){

            Future f = s.shutdown();

            try {
                f.get(20000,TimeUnit.MILLISECONDS);

                System.out.println("Shutdown Server: "+s);

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }

        }

    }


}
