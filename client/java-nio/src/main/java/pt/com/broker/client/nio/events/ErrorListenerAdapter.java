package pt.com.broker.client.nio.events;

import io.netty.channel.Channel;
import pt.com.broker.client.nio.server.HostInfo;
import pt.com.broker.client.nio.utils.ChannelDecorator;
import pt.com.broker.types.NetFault;
import pt.com.broker.types.NetMessage;

/**
 * Created by luissantos on 30-05-2014.
 */
public abstract class ErrorListenerAdapter implements BrokerListener {

    @Override
    public final void deliverMessage(NetMessage message, Channel channel) throws Throwable {


        NetFault netFault = message.getAction().getFaultMessage();

        if(netFault != null){
            onMessage(netFault,((ChannelDecorator)channel).getHost());
        }


    }


    public abstract void onMessage(NetFault message, HostInfo hostInfo);

}
