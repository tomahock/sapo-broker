package pt.com.broker.client.nio.server;


import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.com.broker.client.nio.BaseTest;
import pt.com.broker.client.nio.mocks.ServerFactory;
import pt.com.broker.client.nio.mocks.SocketServer;
import pt.com.broker.client.nio.tests.iptables.IpTables;

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
 * Created by luissantos on 28-05-2014.
 */
public abstract class ServerBaseTest extends BaseTest {

    private static final Logger log = LoggerFactory.getLogger(ServerBaseTest.class);

    int totalServers = 20;

    static IpTables ipTables = new IpTables();

    static  String chainName = "java-nio-tests";

    protected List<SocketServer> getServers() {

        int count = (int) (1 + (Math.random() *  totalServers));

        return getServers(count);
    }

    protected List<SocketServer> getServers(int count) {

        List<SocketServer> servers = new ArrayList<SocketServer>();

        while (count-- > 0){

            SocketServer server = ServerFactory.getInstance(0);

            servers.add(server);

        }

        return servers;
    }



    protected List<SocketServer> getRandomServers(Collection<SocketServer> servers){


        List<SocketServer> _servers = new ArrayList<>(servers);

        Collections.shuffle(_servers);

        return _servers.subList(0, (int) (Math.random() *  _servers.size()));

        //return _servers.subList(0, _servers.size()-2);
    }


    public boolean userHasPermissions(){

        IpTables ipTables = new IpTables();


        return ipTables.hasPermission();

    }

    protected void ShutDownServers(Collection<SocketServer> servers){

        for(SocketServer s : servers){

            Future f = s.shutdown();

            try {
                f.get(20000, TimeUnit.MILLISECONDS);

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


    @BeforeClass()
    public static void setUp() throws Exception {





        if(ipTables.hasPermission()){


            if(!ipTables.addChain(currentChainName())){
                System.out.println("Error adding chain:"+currentChainName());
            }else{
                ipTables.addChaintoChain("OUTPUT",currentChainName());
            }

        }

    }

    @AfterClass()
    public static void tearDown() throws Exception {


        if(ipTables.hasPermission()) {

            ipTables.removeChainfromChain("OUTPUT",currentChainName());

            ipTables.deleteChain(currentChainName());

        }
    }

    public static String currentChainName(){

        String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];

        return chainName+"-"+pid;
    }


}
