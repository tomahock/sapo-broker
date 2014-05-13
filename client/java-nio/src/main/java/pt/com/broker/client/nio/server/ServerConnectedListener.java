package pt.com.broker.client.nio.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.com.broker.client.nio.HostInfo;
import pt.com.broker.client.nio.server.HostContainer;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * Created by luissantos on 13-05-2014.
 */
public class ServerConnectedListener implements ChannelFutureListener {

    private static final Logger log = LoggerFactory.getLogger(ServerConnectedListener.class);

    HostContainer hostContainer;


    public ServerConnectedListener(HostContainer hostContainer) {
        this.hostContainer = hostContainer;
    }

    @Override
    public void operationComplete(ChannelFuture future) throws Exception {

        final Channel channel = future.channel();

        final SocketAddress remoteAddress = channel.remoteAddress();


        InetSocketAddress inetSocketAddress = (InetSocketAddress) remoteAddress;


            if(future.isSuccess()){



            channel.closeFuture().addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {

                    hostContainer.inactiveHost(remoteAddress);

                }
            });


            if(remoteAddress instanceof InetSocketAddress){
                log.debug("Connected to host: "+inetSocketAddress.getHostName() +":"+inetSocketAddress.getPort());
            }

            HostInfo host = hostContainer.getActiveHost(remoteAddress);


            hostContainer.addConnectedHost(host);



        }else{

            log.debug("Error connecting to host: "+inetSocketAddress.getHostName() +":"+inetSocketAddress.getPort());

        }





    }
}
