package pt.com.broker.client.nio.events;

import io.netty.channel.Channel;
import pt.com.broker.client.nio.BrokerClient;
import pt.com.broker.client.nio.server.HostInfo;
import pt.com.broker.client.nio.utils.ChannelDecorator;
import pt.com.broker.types.NetFault;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetNotification;

/**
 * Created by luissantos on 21-04-2014.
 *
 * @author vagrant
 * @version $Id: $Id
 */
public interface BrokerListener {

    /**
     * Fired when a message arrives.
     *
     * @param message The message.
     * @param host a {@link pt.com.broker.client.nio.server.HostInfo} object.
     * @throws java.lang.Throwable if any.
     */
    public void deliverMessage(NetMessage message,HostInfo host)  throws Throwable;


}
