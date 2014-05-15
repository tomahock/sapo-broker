package pt.com.broker.client.nio.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.com.broker.client.nio.HostInfo;
import pt.com.broker.client.nio.bootstrap.BaseBootstrap;
import pt.com.broker.client.nio.server.strategies.RoundRobinStrategy;
import pt.com.broker.client.nio.server.strategies.SelectServerStrategy;
import pt.com.broker.client.nio.utils.CircularContainer;


import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by luissantos on 29-04-2014.
 */
public class HostContainer {

    private static final Logger log = LoggerFactory.getLogger(HostContainer.class);

    private List<HostInfo> hosts;

    private List<HostInfo> connectedHosts;

    private BaseBootstrap bootstrap;

    ExecutorService executorService = Executors.newFixedThreadPool(10);

    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private final CompletionService<HostInfo> service = new ExecutorCompletionService<HostInfo>(executorService);

    SelectServerStrategy strategy = new RoundRobinStrategy();


    public HostContainer(BaseBootstrap bootstrap) {
        this(1, bootstrap);
    }

    public HostContainer(int capacity, BaseBootstrap bootstrap) {

        this.bootstrap = bootstrap;

        hosts = new ArrayList<HostInfo>(capacity);

        connectedHosts = new ArrayList<HostInfo>(capacity);

        strategy.setCollection(connectedHosts);

    }


    public void add(HostInfo host) {
        hosts.add(host);
    }

    public int size() {
        return hosts.size();
    }


    private Future<HostInfo> connect(HostInfo server) {

        Collection<HostInfo> servers = new ArrayList<HostInfo>();

        servers.add(server);

        return connect(servers);
    }

    public Future<HostInfo> connect() {

        ArrayList<HostInfo> hosts = notConnectedHosts();

        Future f = connect(hosts);

        startReconnectThread();

        return f;
    }

    private Future<HostInfo> connect(final Collection<HostInfo> servers) {

        final EventLoopGroup eventLoop = bootstrap.getGroup();


        return eventLoop.submit(new Callable<HostInfo>() {

            @Override
            public HostInfo call() throws Exception {



                    for (final HostInfo host : servers) {

                        service.submit(new Callable<HostInfo>() {

                            @Override
                            public HostInfo call() throws Exception {

                                ChannelFuture f = connectToHost(host);

                                f.awaitUninterruptibly();

                                return f.isSuccess() ? host : null;


                            }

                        });

                    }


                    HostInfo host = null;

                    int count = servers.size();

                    do {

                        host = service.take().get();

                        count--;
                    } while ((host == null || !host.isActive()) && count > 0);


                    return host;

                }



        });


    }

    private void registerSuccessfulConnect(Channel channel, final HostInfo hostInfo) throws Exception {


        final HostContainer hostContainer = this;

        hostInfo.setChannel(channel);

        this.addConnectedHost(hostInfo);

        channel.closeFuture().addListener(new ChannelFutureListener() {

            @Override
            public void operationComplete(ChannelFuture future) throws Exception {

                log.debug("Server disconnected: " + hostInfo);

                hostContainer.inactiveHost(hostInfo);

            }

        });

    }

    public ArrayList<HostInfo> notConnectedHosts() {

        ArrayList<HostInfo> list = new ArrayList<HostInfo>(hosts.size());

        for(HostInfo host : hosts){
            if(host.getStatus() == HostInfo.STATUS.CLOSED){
                list.add(host);
            }
        }

        return list;
    }


    protected boolean isConnected(HostInfo hostInfo) {
        return hostInfo!=null && hostInfo.getStatus() == HostInfo.STATUS.OPEN && connectedHosts.contains(hostInfo);
    }

    protected HostInfo inactiveHost(HostInfo host) {

        if (host != null) {
            synchronized (hosts) {
                connectedHosts.remove(host);
                host.setChannel(null);
                host.setStatus(HostInfo.STATUS.CLOSED);
            }
        }

        return host;

    }


    public Channel getAvailableChannel() {

        HostInfo host = strategy.next();

        if (host != null) {
            return host.getChannel();
        }

        return null;

    }

    public ChannelFuture disconnect(HostInfo host){

        if(!isConnected(host)){
            return null;
        }

        Channel channel = host.getChannel();

        if(channel == null){
            return null;
        }

        host.setChannel(null);

        return channel.disconnect();

    }

    private ChannelFuture connectToHost(final HostInfo host) throws Exception {

        final ChannelFuture f = bootstrap.connect(host);

        host.setStatus(HostInfo.STATUS.CONNECTING);

        f.addListener( new ChannelFutureListener() {

            @Override
            public void operationComplete(ChannelFuture future) throws Exception {

                if(future.isSuccess()){

                    log.debug("Connected to server: "+ host);

                    registerSuccessfulConnect(f.channel(), host);
                    host.setStatus(HostInfo.STATUS.OPEN);


                }else{
                    host.setStatus(HostInfo.STATUS.CLOSED);
                    log.debug("Error connecting to server: "+ host);
                }

            }

        });



        return f;
    }


    private void startReconnectThread(){

        scheduler.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {

                synchronized (hosts) {

                    List<HostInfo> notConnectedHosts = notConnectedHosts();

                    if (notConnectedHosts.size()>0) {

                        log.debug("CONNECT ALL THE SERVERS");



                        for (HostInfo host : notConnectedHosts ) {

                            Future f = connect(host);

                            try {
                                f.get();

                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            }

                            log.debug("connected");

                        }
                    }

                }


            }
        }, 2000, 1000, TimeUnit.MILLISECONDS);

    }

    protected void addConnectedHost(HostInfo host) throws Exception {

        if(host == null){
            throw new Exception("Invalid host");
        }

        connectedHosts.add(host);
    }

    public Collection<HostInfo> getConnectedHosts(){
        return connectedHosts;
    }

}
