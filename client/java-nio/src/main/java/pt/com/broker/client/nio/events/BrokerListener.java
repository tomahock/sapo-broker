package pt.com.broker.client.nio.events;

import io.netty.channel.Channel;
import pt.com.broker.client.nio.BrokerClient;
import pt.com.broker.client.nio.utils.ChannelDecorator;
import pt.com.broker.types.NetFault;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetNotification;

/**
 * Created by luissantos on 21-04-2014.
 */
public interface BrokerListener {

    /**
    * Fired when a message arrives.
    *
    * @param message The message.
    */
    public void deliverMessage(NetMessage message,Channel channel)  throws Throwable;


}
