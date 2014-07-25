package pt.com.broker.client.nio;

import io.netty.channel.ChannelPromise;
/**
 * Created by luissantos on 23-04-2014.
 *
 * @author vagrant
 * @version $Id: $Id
 */
public class NioSocketChannelBroker extends io.netty.channel.socket.nio.NioSocketChannel{


    /** {@inheritDoc} */
    @Override
    public ChannelPromise newPromise() {
        return super.newPromise();
    }
}
