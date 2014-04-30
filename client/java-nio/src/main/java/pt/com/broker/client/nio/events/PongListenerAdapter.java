package pt.com.broker.client.nio.events;

import io.netty.channel.Channel;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetPong;

/**
 * Created by luissantos on 30-04-2014.
 */
public abstract class PongListenerAdapter implements BrokerListener {


    @Override
    public void deliverMessage(NetMessage message, Channel channel) throws Throwable {

        NetPong netPong = message.getAction().getPongMessage();

        this.onMessage(netPong);

    }


    public abstract void onMessage(NetPong message);
}
