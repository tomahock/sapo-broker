package pt.com.broker.client.nio.events;

import io.netty.channel.Channel;
import pt.com.broker.client.nio.server.HostInfo;
import pt.com.broker.client.nio.utils.ChannelDecorator;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetPong;

/**
 * Created by luissantos on 30-04-2014.
 */
public abstract class PongListenerAdapter implements BrokerListener {


    @Override
    public final void deliverMessage(NetMessage message, HostInfo host) throws Throwable {

        NetPong netPong = message.getAction().getPongMessage();

        if(netPong==null) {
            return;
        }

        this.onMessage(netPong, host);

    }

    public abstract void onMessage(NetPong message, HostInfo hostInfo);


}
