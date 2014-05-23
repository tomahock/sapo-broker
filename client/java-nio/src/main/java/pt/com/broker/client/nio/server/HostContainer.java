package pt.com.broker.client.nio.server;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.util.AttributeKey;
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
import java.util.Observable;
import java.util.concurrent.*;

/**
 * Created by luissantos on 29-04-2014.
 */
public class HostContainer extends Observable {

    private static final Logger log = LoggerFactory.getLogger(HostContainer.class);

    public static final AttributeKey<HostInfo> ATTRIBUTE_HOST_INFO  = AttributeKey.valueOf("HOST-INFO");

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

    public HostInfo connect() {

        Future f = connectAsync();

        try {

            return (HostInfo) f.get();

        } catch (Exception e) {

            throw  new RuntimeException("Could not connect",e);

        }

    }

    public Future<HostInfo> connectAsync() {

        synchronized (hosts) {

            ArrayList<HostInfo> hosts = notConnectedHosts();

            Future f = connect(hosts);

            return f;
        }
    }

    private Future<HostInfo> connect(HostInfo server) {

        synchronized (hosts) {

            Collection<HostInfo> servers = new ArrayList<HostInfo>();

            servers.add(server);

            return connect(servers);
        }
    }

    private Future<HostInfo> connect(final Collection<HostInfo> servers) {

        return executorService.submit(new Callable<HostInfo>() {

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


                    if(host == null){
                        throw new Exception("Could not connect");
                    }

                    return host;

                }



        });


    }

    private void reconnect(final HostInfo host){

        if(!scheduler.isShutdown() && !bootstrap.getGroup().isShuttingDown()) {

            final HostContainer hostContainer = this;

            scheduler.schedule(new Runnable() {
                @Override
                public void run() {

                    try {

                        connectToHost(host).addListener(new ChannelFutureListener() {

                            @Override
                            public void operationComplete(ChannelFuture future) throws Exception {

                                if(!future.isSuccess()){
                                    return;
                                }

                                synchronized (hostContainer) {
                                    log.debug("NotifyObservers: " + host);

                                    hostContainer.setChanged();
                                    hostContainer.notifyObservers(new ReconnectEvent(host));

                                }

        this.addConnectedHost(hostInfo);

                            }

                        });


                    } catch (Exception e) {


                    if(!future.isCancelled()) {
                       reconnect(hostInfo);
                    }

            }


                }
            }, 2000, TimeUnit.MILLISECONDS);
        }

    }

    public ArrayList<HostInfo> notConnectedHosts() {

        synchronized (hosts) {

            ArrayList<HostInfo> list = new ArrayList<HostInfo>(0);

            for (HostInfo host : hosts) {

                synchronized (host) {

                    if (host.getStatus().equals(HostInfo.STATUS.CLOSED)) {
                        list.add(host);
                    }
                }
            }

            return list;
        }


    }


    protected boolean isConnected(HostInfo hostInfo) {
        return hostInfo!=null && hostInfo.getStatus() == HostInfo.STATUS.OPEN && connectedHosts.contains(hostInfo);
    }

    protected HostInfo inactiveHost(final HostInfo host) {

        if (host != null) {

            synchronized (connectedHosts) {
                connectedHosts.remove(host);
            }

            host.setChannel(null);
            host.setStatus(HostInfo.STATUS.CLOSED);
            log.debug("Server disconnected: " + host);

        }


        return host;

    }


    /**
     * Gets an available channel. If there are no available channel it will loop until no server is available.
     * If its not possible to write in channel for a while the server will be disconnected.
     * @see  pt.com.broker.client.nio.codecs.HeartbeatHandler
     *
     * @return Channel
     * @throws InterruptedException
     */
    public synchronized Channel getAvailableChannel() throws InterruptedException {

        HostInfo host = null;

        int total = connectedHosts.size();

        do {

            host = strategy.next();

            if(host == null && total-- < 1){

                Thread.sleep(500);

                total = connectedHosts.size();

                if(total==0){
                    return null;
                }

            }

        } while (host == null || (host != null && !host.getChannel().isWritable()));


        return host.getChannel();


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

                synchronized (host) {

                    if (future.isSuccess()) {

                    if (!future.isSuccess()) {

                        host.setStatus(HostInfo.STATUS.CLOSED);


                    } else {

                        host.setStatus(HostInfo.STATUS.CLOSED);
                        log.debug("Error connecting to server: " + host);

                        reconnect(host);

                        return ;

                    }

                    Channel channel = f.channel();
                    channel.attr(ATTRIBUTE_HOST_INFO).set(host);

                    host.setChannel(channel);

                    host.setStatus(HostInfo.STATUS.OPEN);
                    addConnectedHost(host);

                    log.debug("Connected to server: " + host);

                    channel.closeFuture().addListener(new ChannelFutureListener() {

                        @Override
                        public void operationComplete(ChannelFuture future) throws Exception {

                            inactiveHost(host);

                            if(!future.isCancelled()) {
                                reconnect(host);
                            }

                        }

                    });




                }

            }

        });



        return f;
    }

    protected void addConnectedHost(HostInfo host) throws Exception {

        if(host == null){
            throw new Exception("Invalid host");
        }

        synchronized (connectedHosts){
            connectedHosts.add(host);
        }

    }

    public Collection<HostInfo> getConnectedHosts(){
        return connectedHosts;
    }

    public int getHostsSize(){
        synchronized (hosts){
            return hosts.size();
        }
    }

    public int getConnectedSize(){

        synchronized (connectedHosts){
            return connectedHosts.size();
        }

    }

    public void shutdown(){
        scheduler.shutdown();
        executorService.shutdown();
    }





}
