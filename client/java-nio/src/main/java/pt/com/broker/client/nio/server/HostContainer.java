package pt.com.broker.client.nio.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.com.broker.client.nio.HostInfo;
import pt.com.broker.client.nio.bootstrap.BaseBootstrap;
import pt.com.broker.client.nio.utils.CircularContainer;


import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.*;

/**
 * Created by luissantos on 29-04-2014.
 */
public class HostContainer {

    private static final Logger log = LoggerFactory.getLogger(HostContainer.class);

    private CircularContainer<HostInfo> hosts;

    private CircularContainer<HostInfo> connectedHosts;

    private BaseBootstrap bootstrap;

    private final CompletionService<HostInfo> service;

    public HostContainer(BaseBootstrap bootstrap) {
        this(1, bootstrap);
    }

    public HostContainer(int capacity, BaseBootstrap bootstrap) {

        hosts = new CircularContainer<HostInfo>(capacity);
        connectedHosts = new CircularContainer<HostInfo>(capacity);
        this.bootstrap = bootstrap;

        service = new ExecutorCompletionService<HostInfo>(bootstrap.getGroup());
    }

    public ArrayList<HostInfo> getInnerContainer() {
        return connectedHosts.getInnerContainer();
    }

    public void add(HostInfo host) {
        hosts.add(host);
    }

    public int size() {
        return hosts.size();
    }


    public Future<HostInfo> connect() {

        final EventLoopGroup eventLoop = bootstrap.getGroup();

        final ServerConnectedListener listener = new ServerConnectedListener(this);

        return eventLoop.submit(new Callable<HostInfo>() {

            @Override
            public HostInfo call() throws Exception {

                ArrayList<HostInfo> hosts = notConnectedHosts();

                for (final HostInfo host : hosts) {


                    service.submit(new Callable<HostInfo>() {

                        @Override
                        public HostInfo call() throws Exception {

                            ChannelFuture f = connectToHost(host).addListener(listener);

                            f.awaitUninterruptibly();

                            if (!f.isSuccess()) {
                                return null;
                            }


                            host.setChannelFuture(f);

                            return host;


                        }

                    });

                }


                int total = hosts.size();

                while (total-- > 0) {

                    HostInfo host = service.take().get();

                    if (host != null && host.isActive()) {
                        return host;
                    }
                }


                return null;


            }

        });


    }

    public ArrayList<HostInfo> notConnectedHosts() {

        ArrayList<HostInfo> list = new ArrayList<HostInfo>(hosts.getInnerContainer());

        list.removeAll(connectedHosts.getInnerContainer());


        return list;
    }


    protected HostInfo getActiveHost(SocketAddress socketAddress) {

        for (HostInfo host : connectedHosts.getInnerContainer()) {

            if (host.getChannel().remoteAddress().equals(socketAddress)) {
                return host;
            }

        }

        return null;
    }

    protected HostInfo getActiveHost(String hostname, int port) {

        return getActiveHost(new InetSocketAddress(hostname, port));
    }

    protected HostInfo inactiveHost(SocketAddress socketAddress) {

        HostInfo host = getActiveHost(socketAddress);

        if (host != null) {
            connectedHosts.remove(host);
        }


        return host;
    }


    public Channel getActiveChannel(String hostname, int port) {

        HostInfo host = getActiveHost(hostname, port);

        if (host == null) {
            return null;
        }


        return host.getChannel();
    }

    public Channel getAvailableChannel() {

        HostInfo host = connectedHosts.get();

        Channel c = host.getChannel();

        if (c != null) {
            return c;
        }

        Future<HostInfo> future = connect();


        try {

            return future.get().getChannel();

        } catch (Throwable e) {

            e.printStackTrace();

        }

        return null;

    }

    protected ChannelFuture connectToHost(final HostInfo host) {

        ChannelFuture f = bootstrap.connect(host);

        return f;
    }


    protected void addConnectedHost(HostInfo host) {
        connectedHosts.add(host);
    }

}
