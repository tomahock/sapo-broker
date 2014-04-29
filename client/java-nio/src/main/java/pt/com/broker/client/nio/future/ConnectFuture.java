package pt.com.broker.client.nio.future;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import pt.com.broker.client.nio.BrokerClient;
import pt.com.broker.client.nio.HostInfo;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by luissantos on 29-04-2014.
 */
public class ConnectFuture extends BrokerFuture {

    public ConnectFuture(BrokerClient bk) {
        super(bk);
    }

    @Override
    public boolean cancel(boolean b) {

        for(HostInfo host : bk.getHosts().getInnerContainer()){
            ChannelFuture cf = host.getChannelFuture();
                if(!cf.cancel(b)){
                    return false;
                }
        }

        return true;
    }

    @Override
    public boolean isCancelled() {
        for(HostInfo host : bk.getHosts().getInnerContainer()){
            ChannelFuture cf = host.getChannelFuture();
            if(!cf.isCancelled()){
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isDone() {

        for(HostInfo host : bk.getHosts().getInnerContainer()){

            ChannelFuture cf = host.getChannelFuture();

            if(cf != null && cf.isDone()){
                Channel channel = cf.channel();

                if(channel.isOpen()){
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException {
        return null;
    }

    @Override
    public Object get(long l, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
        return null;
    }
}
