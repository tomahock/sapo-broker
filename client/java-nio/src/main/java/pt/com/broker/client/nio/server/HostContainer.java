package pt.com.broker.client.nio.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.com.broker.client.nio.bootstrap.BaseBootstrap;
import pt.com.broker.client.nio.server.strategies.RoundRobinStrategy;
import pt.com.broker.client.nio.server.strategies.SelectServerStrategy;
import pt.com.broker.client.nio.utils.ChannelDecorator;

import java.util.*;
import java.util.concurrent.*;

/**
 * Created by luissantos on 29-04-2014.
 *
 * @author vagrant
 * @version $Id: $Id
 */
public class HostContainer extends Observable {

    private static final Logger log = LoggerFactory.getLogger(HostContainer.class);

    private static final Object channelLocker = new Object();

    private List<HostInfo> hosts;

    private List<HostInfo> connectedHosts;

    private BaseBootstrap bootstrap;

    ExecutorService executorService = Executors.newFixedThreadPool(10);

    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);


    private final CompletionService<HostInfo> service = new ExecutorCompletionService<HostInfo>(executorService);

    SelectServerStrategy strategy = new RoundRobinStrategy();


    /**
     * <p>Constructor for HostContainer.</p>
     *
     * @param bootstrap a {@link pt.com.broker.client.nio.bootstrap.BaseBootstrap} object.
     */
    public HostContainer(BaseBootstrap bootstrap) {
        this(1, bootstrap);
    }

    /**
     * <p>Constructor for HostContainer.</p>
     *
     * @param capacity a int.
     * @param bootstrap a {@link pt.com.broker.client.nio.bootstrap.BaseBootstrap} object.
     */
    public HostContainer(int capacity, BaseBootstrap bootstrap) {

        this.bootstrap = bootstrap;

        hosts = new ArrayList<HostInfo>(capacity);

        connectedHosts = new ArrayList<HostInfo>(capacity);

        strategy.setCollection(connectedHosts);


    }


    /**
     * <p>add.</p>
     *
     * @param host a {@link pt.com.broker.client.nio.server.HostInfo} object.
     */
    public void add(HostInfo host) {

        synchronized (hosts) {

            if (hosts.contains(host)) {
                throw new RuntimeException("Cannot add server twice");
            }

            hosts.add(host);
        }

    }

    /**
     * <p>size.</p>
     *
     * @return a int.
     */
    public int size() {
        return hosts.size();
    }

    /**
     * <p>connect.</p>
     *
     * @return a {@link pt.com.broker.client.nio.server.HostInfo} object.
     */
    public HostInfo connect() {

        Future<HostInfo> f = connectAsync();

        try {

            return f.get();

        } catch (Exception e) {

            throw new RuntimeException("Could not connect", e);

        }

    }

    /**
     * <p>connectAsync.</p>
     *
     * @return a {@link java.util.concurrent.Future} object.
     */
    public Future<HostInfo> connectAsync() {


        synchronized (hosts) {

            ArrayList<HostInfo> hosts = getClosedHosts();

            if (hosts.size() == 0) {
                throw new RuntimeException("There are no available hosts to connect");
            }

            Future<HostInfo> f = connect(hosts);

            return f;
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

                            final CountDownLatch latch = new CountDownLatch(1);

                            f.addListener(new ChannelFutureListener() {
                                @Override
                                public void operationComplete(ChannelFuture future) throws Exception {

                                    latch.countDown();

                                }
                            });

                            latch.await();

                            return host;


                        }

                    });

                }


                HostInfo host = null;

                int count = servers.size();


                    /* @todo server connected and isWritable */
                do {

                    host = service.take().get();

                    count--;
                } while ((host == null || !host.isActive()) && count > 0);


                if (host == null) {
                    throw new Exception("Could not connect");
                }

                while (!host.isActive()) {
                    Thread.sleep(500);
                }

                return host;

            }


        });


    }

    private void reconnect(final HostInfo host) {

        if (!scheduler.isShutdown() && !bootstrap.getGroup().isShuttingDown()) {

            final HostContainer hostContainer = this;

            scheduler.schedule(new Runnable() {
                @Override
                public void run() {

                    try {

                        connectToHost(host).addListener(new ChannelFutureListener() {

                            @Override
                            public void operationComplete(ChannelFuture future) throws Exception {

                                if (!future.isSuccess()) {
                                    return;
                                }

                                synchronized (hostContainer) {
                                    log.debug("NotifyObservers: " + host);
                                    hostContainer.setChanged();
                                    hostContainer.notifyObservers(new ReconnectEvent(host));
                                }

                            }

                        });


                    } catch (Exception e) {

                    }


                }
            }, 2000, TimeUnit.MILLISECONDS);
        }

    }

    /**
     * <p>getClosedHosts.</p>
     *
     * @return a {@link java.util.ArrayList} object.
     */
    public ArrayList<HostInfo> getClosedHosts() {

        synchronized (hosts) {

            ArrayList<HostInfo> list = new ArrayList<HostInfo>(0);

            for (HostInfo host : hosts) {

                synchronized (host) {

                    if (host.getStatus() == HostInfo.STATUS.CLOSED) {
                        list.add(host);
                    }
                }
            }

            return list;
        }

    }

    /**
     * <p>notConnectedHosts.</p>
     *
     * @return a {@link java.util.ArrayList} object.
     */
    public ArrayList<HostInfo> notConnectedHosts() {

        synchronized (hosts) {

            ArrayList<HostInfo> list = new ArrayList<HostInfo>(0);

            for (HostInfo host : hosts) {

                synchronized (host) {

                    if (host.getStatus() == HostInfo.STATUS.CLOSED || host.getStatus() == HostInfo.STATUS.CONNECTING
                            || host.getStatus() == HostInfo.STATUS.DISABLE) {
                        list.add(host);
                    }
                }
            }

            return list;
        }


    }


    /**
     * <p>isConnected.</p>
     *
     * @param hostInfo a {@link pt.com.broker.client.nio.server.HostInfo} object.
     * @return a boolean.
     */
    protected boolean isConnected(HostInfo hostInfo) {
        return hostInfo != null && hostInfo.getStatus() == HostInfo.STATUS.OPEN && connectedHosts.contains(hostInfo);
    }

    /**
     * <p>inactiveHost.</p>
     *
     * @param host a {@link pt.com.broker.client.nio.server.HostInfo} object.
     * @return a {@link pt.com.broker.client.nio.server.HostInfo} object.
     */
    protected HostInfo inactiveHost(final HostInfo host) {

        if (host != null) {

            synchronized (connectedHosts) {

                if (!connectedHosts.remove(host)) {
                    throw new RuntimeException("invalid host removed");
                }

                log.debug("Connection closed: " + host);
            }

        }


        return host;

    }


    /**
     * Gets an available Host. If there are no available host the code  will loop until no server is available.
     * If its not possible to write in channel for a while the server will be disconnected.
     *
     * @return Channel
     * @throws java.lang.InterruptedException if any.
     * @see pt.com.broker.client.nio.codecs.HeartbeatHandler
     */
    public HostInfo getAvailableHost() throws InterruptedException {

        HostInfo host = null;

        int total = connectedHosts.size();

        do {

            host = strategy.next();

            if (host == null && total-- < 1) {


                total = connectedHosts.size();

                if (total == 0) {
                    return null;
                }

            }

        } while (host == null || (host != null && !host.getChannel().isOpen()));


        return host;


    }

    /**
     * <p>disconnect.</p>
     *
     * @return a {@link java.util.concurrent.Future} object.
     */
    public Future disconnect() {


        return executorService.submit(new Runnable() {

            @Override
            public void run() {

                synchronized (hosts) {


                    for (final HostInfo host : hosts) {

                        try {

                            synchronized (host) {

                                if(host.getChannel()!=null){

                                    disconnect(host).get();
                                }

                                host.setStatus(HostInfo.STATUS.DISABLE);

                            }

                        } catch (Throwable e) {
                            log.error("Error disconnecting",e);
                        }

                    }

                }

            }
        });


    }

    /**
     * <p>disconnect.</p>
     *
     * @param host a {@link pt.com.broker.client.nio.server.HostInfo} object.
     * @return a {@link io.netty.channel.ChannelFuture} object.
     */
    public ChannelFuture disconnect(final HostInfo host) {



            if(host.getStatus() == HostInfo.STATUS.DISABLE){
                throw new RuntimeException("Server already disconnected");
            }

            Channel channel = host.getChannel();

            host.setChannel(null);

            ChannelFuture f = channel.disconnect();

            f.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    log.debug("Server disconnected");
                }
            });


            return f;

    }

    private ChannelFuture connectToHost(final HostInfo host) throws Exception {


        synchronized (host) {

            if (host.getStatus() != HostInfo.STATUS.CLOSED) {
                throw new RuntimeException("Cannot open an host that is not closed");
            }

            host.setStatus(HostInfo.STATUS.CONNECTING);

            final ChannelFuture f = bootstrap.connect(host);


            f.addListener(new ChannelFutureListener() {

                @Override
                public void operationComplete(ChannelFuture future) throws Exception {

                    synchronized (host) {
                        if (!future.isSuccess()) {

                            host.reconnectAttempt();

                            log.debug("Error connecting to server: " + host);
                            reconnect(host);

                            return;
                        }

                        Channel channel = new ChannelDecorator(f.channel());


                        host.resetReconnectLimit();

                        channel.closeFuture().addListener(new ChannelFutureListener() {

                            @Override
                            public void operationComplete(ChannelFuture future) throws Exception {

                                inactiveHost(host);

                                if (!future.isCancelled()) {
                                    reconnect(host);
                                }
                            }

                        });

                        addConnectedHost(host);

                        log.debug("Connected to server: " + host);


                    }

                }


            });
            return f;


        }
    }


    /**
     * <p>addConnectedHost.</p>
     *
     * @param host a {@link pt.com.broker.client.nio.server.HostInfo} object.
     * @throws java.lang.Exception if any.
     */
    protected void addConnectedHost(HostInfo host) throws Exception {

        if (host == null) {
            throw new Exception("Invalid host");
        }

        synchronized (connectedHosts) {

            if (connectedHosts.contains(host)) {
                throw new RuntimeException("Cannot add connected server twice");
            }

            connectedHosts.add(host);

        }

    }

    /**
     * <p>Getter for the field <code>connectedHosts</code>.</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    public Collection<HostInfo> getConnectedHosts() {
        return connectedHosts;
    }

    /**
     * <p>getHostsSize.</p>
     *
     * @return a int.
     */
    public int getHostsSize() {
        synchronized (hosts) {
            return hosts.size();
        }
    }

    /**
     * <p>getConnectedSize.</p>
     *
     * @return a int.
     */
    public int getConnectedSize() {

        synchronized (connectedHosts) {
            return connectedHosts.size();
        }

    }

    /**
     * <p>shutdown.</p>
     */
    public void shutdown() {
        scheduler.shutdown();
        executorService.shutdown();
    }


}
