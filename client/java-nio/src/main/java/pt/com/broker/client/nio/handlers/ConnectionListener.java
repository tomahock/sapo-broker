package pt.com.broker.client.nio.handlers;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.AttributeKey;
import pt.com.broker.client.nio.HostInfo;
import pt.com.broker.client.nio.utils.HostContainer;

/**
 * Created by luissantos on 29-04-2014.
 */
public class ConnectionListener implements ChannelFutureListener {

    HostContainer hostContainer;

    final AttributeKey<HostInfo> host = new AttributeKey<HostInfo>("hostinfo");

    public ConnectionListener(HostContainer hostContainer) {

        this.hostContainer = hostContainer;
    }

    @Override
    public void operationComplete(ChannelFuture future) throws Exception {

        if(!future.isSuccess()){

            Channel c = future.channel();

            HostInfo host = (HostInfo) c.attr(this.host).get();

            //hostContainer.reconnect(host);

        }

    }
}
