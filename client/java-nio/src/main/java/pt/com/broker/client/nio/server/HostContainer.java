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

    ExecutorService executorService = Executors.newFixedThreadPool(10);

    private final CompletionService<HostInfo> service;

    public HostContainer(BaseBootstrap bootstrap) {
        this(1, bootstrap);
    }

    public HostContainer(int capacity, BaseBootstrap bootstrap) {

        hosts = new CircularContainer<HostInfo>(capacity);
        connectedHosts = new CircularContainer<HostInfo>(capacity);
        this.bootstrap = bootstrap;

        service = new ExecutorCompletionService<HostInfo>(executorService);
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


        final  HostContainer hostContainer = this;

        return eventLoop.submit(new Callable<HostInfo>() {

            @Override
            public HostInfo call() throws Exception {

                ArrayList<HostInfo> hosts = notConnectedHosts();

                for (final HostInfo host : hosts) {


                    service.submit(new Callable<HostInfo>() {

                        @Override
                        public HostInfo call() throws Exception {

                            log.debug("Connetecting...."+host);

                            ChannelFuture f = connectToHost(host);

                            f.awaitUninterruptibly();

                            if (!f.isSuccess()) {
                                log.debug("Error");
                                return null;
                            }

                            log.debug("Success");


                            host.setChannelFuture(f);

                            registerSuccessfulConnect(f.channel(),host);

                            return host;


                        }

                    });

                }



                HostInfo host = null;

                int count = hosts.size();

                do{

                    host = service.take().get();

                    count--;
                }while ( (host == null || !host.isActive()) && count > 0 );





                return host;


            }

        });


    }

    private void registerSuccessfulConnect(Channel channel, final HostInfo hostInfo) throws Exception {

        final HostContainer hostContainer = this;

        this.addConnectedHost(hostInfo);

        channel.closeFuture().addListener(new ChannelFutureListener() {

            @Override
            public void operationComplete(ChannelFuture future2) throws Exception {

                SocketAddress address = future2.channel().remoteAddress();

                log.debug("Server disconnected: " + address);

                hostContainer.inactiveHost(hostInfo);

            }

        });

    }

    public ArrayList<HostInfo> notConnectedHosts() {

        if(connectedHosts.size() == 0 ){
            return  new ArrayList<HostInfo>(hosts.getInnerContainer());
        }

        ArrayList<HostInfo> list = new ArrayList<HostInfo>(hosts.getInnerContainer());

        list.removeAll(connectedHosts.getInnerContainer());

        log.debug("Not Connected Size:"+list.size());

        return list;
    }



    protected HostInfo getHost(SocketAddress socketAddress){


        for(HostInfo host : hosts.getInnerContainer()){

            if(host.getChannel().remoteAddress().equals(socketAddress)){
                return host;
            }

        }

        return null;
    }


    protected HostInfo getActiveHost(SocketAddress socketAddress) {

        for (HostInfo host : connectedHosts.getInnerContainer()) {

            if (host!=null  && host.getChannel().remoteAddress().equals(socketAddress)) {
                return host;
            }

        }

        return null;
    }

    protected HostInfo getActiveHost(String hostname, int port) {

        return getActiveHost(new InetSocketAddress(hostname, port));
    }


    protected HostInfo inactiveHost(HostInfo host) {


        log.debug("Disabling: "+host);

        if (host != null) {
            connectedHosts.remove(host);
        }

        return host;

    }

    protected HostInfo inactiveHost(SocketAddress socketAddress) {

        HostInfo host = getActiveHost(socketAddress);

        return inactiveHost(host);
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


    protected void addConnectedHost(HostInfo host) throws Exception {

        if(host == null){
            throw new Exception("Invalid host");
        }

        connectedHosts.add(host);
    }

    public Collection<HostInfo> getConnectedHosts(){
        return connectedHosts.getInnerContainer();
    }

}
