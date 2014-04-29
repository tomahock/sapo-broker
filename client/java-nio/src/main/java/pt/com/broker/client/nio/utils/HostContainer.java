package pt.com.broker.client.nio.utils;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.com.broker.client.nio.HostInfo;
import pt.com.broker.client.nio.bootstrap.Bootstrap;


import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.*;

/**
 * Created by luissantos on 29-04-2014.
 */
public class HostContainer {

    private static final Logger log = LoggerFactory.getLogger(HostContainer.class);

    private CircularContainer<HostInfo> innerContainer;

    private CircularContainer<HostInfo> activeHosts;

    private Bootstrap bootstrap;

    public HostContainer(Bootstrap bootstrap) {
        this(1, bootstrap);
    }

    public HostContainer(int capacity, Bootstrap bootstrap) {
        innerContainer = new CircularContainer<HostInfo>(capacity);
        activeHosts = new CircularContainer<HostInfo>(capacity);
        this.bootstrap = bootstrap;
    }

    public HostContainer(Collection<HostInfo> elements, Bootstrap bootstrap) {
        innerContainer = new CircularContainer<HostInfo>(elements);
        activeHosts = new CircularContainer<HostInfo>(elements);
        this.bootstrap = bootstrap;
    }


    public ArrayList<HostInfo> getInnerContainer() {
        return activeHosts.getInnerContainer();
    }

    public void add(HostInfo host) {
        innerContainer.add(host);
    }

    public int size() {
        return innerContainer.size();
    }


    public Future<HostInfo> connect() {


        final EventLoopGroup eventLoop = bootstrap.getBootstrap().group();

        final ArrayList<HostInfo> hosts = notConnectedHosts();




        return eventLoop.submit(new Callable<HostInfo>() {

                                    @Override
                                    public HostInfo call() throws Exception {


                                        CompletionService<HostInfo> service = new ExecutorCompletionService<HostInfo>(eventLoop);


                                        for (final HostInfo host : hosts) {


                                            service.submit(new Callable<HostInfo>() {

                                                   @Override
                                                   public HostInfo call() throws Exception {

                                                       ChannelFuture f = bootstrap.connect(host);

                                                       activeHosts.add(host);

                                                       try {

                                                           f.get();

                                                           if(f.isSuccess()){

                                                               host.resetReconnectLimit();

                                                           }else{
                                                               host.reconnectAttempt();
                                                               log.debug("Error connecting host");
                                                           }



                                                       } catch (Throwable throwable) {

                                                           log.debug("Error connecting");

                                                           host.reconnectAttempt();

                                                           return null;
                                                       }


                                                       return host;
                                                   }

                                            });

                                        }


                                        for (int t = 0, n = hosts.size(); t < n; t++) {
                                            HostInfo host = service.take().get();

                                            if (host.isActive()) {
                                                return host;
                                            }
                                        }


                                        return null;


                                    }

                                }
        );


    }

    public ArrayList<HostInfo> notConnectedHosts() {

        ArrayList<HostInfo> list = new ArrayList<HostInfo>();

        for (HostInfo host : innerContainer.getInnerContainer()) {

            if (activeHosts.getInnerContainer().indexOf(host) < 0) {

                list.add(host);

            }
        }


        return list;
    }

    protected HostInfo getActiveHost(String hostname, int port){

        for(HostInfo host : activeHosts.getInnerContainer()){

            if(host.getHostname() == hostname && host.getPort() == port){
                return host;
            }
        }


        return null;
    }



    public Channel getActiveChannel(String hostname, int port){

        HostInfo host = getActiveHost(hostname,port);

        if(host!=null){
            return host.getChannel();
        }


        return null;
    }

    public Channel getActiveChannel() {


        int size = activeHosts.size();

        log.debug("Active hosts size: "+size);

        Channel c = null;

        while (size > 0) {

            HostInfo host = activeHosts.get();

            if (host.isActive()) {

                c = host.getChannel();

                break;

            } else {


                reconnect(host);

                c = getActiveChannel();
            }

            size--;
        }


        if(c!=null){
            return c;
        }



        Future<HostInfo> host = connect();


        try {

            return host.get().getChannel();


        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }


        return null;

    }


    protected ChannelFuture reconnect(HostInfo host){

        log.debug("Reconnecting "+host);


        activeHosts.remove(host);

        ChannelFuture f = bootstrap.connect(host);

        activeHosts.add(host);

        return f;

    }

}
