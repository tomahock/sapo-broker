package pt.com.broker.client.nio.events;

import io.netty.channel.Channel;
import pt.com.broker.client.nio.BrokerClient;
import pt.com.broker.client.nio.HostInfo;
import pt.com.broker.client.nio.server.HostContainer;
import pt.com.broker.types.NetAccepted;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetFault;
import pt.com.broker.types.NetMessage;

/**
 * Created by luissantos on 09-05-2014.
 */
abstract public class MessageAcceptedAdapter implements BrokerListener {


    abstract public void onMessage(NetAccepted message, HostInfo host);

    abstract public void onFault(NetFault fault, HostInfo host);

    abstract public void onTimeout(String actionID);

    @Override
    final public void deliverMessage(NetMessage message, Channel channel) throws Throwable {

        if(message.getAction().getActionType() == NetAction.ActionType.ACCEPTED){
            onMessage(message.getAction().getAcceptedMessage(),getHostFromChannel(channel));
            return;
        }


        if(message.getAction().getActionType() == NetAction.ActionType.FAULT){
            onFault(message.getAction().getFaultMessage(),getHostFromChannel(channel));
            return;
        }


        throw new RuntimeException("Invalid message");

    }

    private HostInfo getHostFromChannel(Channel channel){

        HostInfo h = (HostInfo) channel.attr(HostContainer.ATTRIBUTE_HOST_INFO).get();


        return h;

    }

    @Override
    final public void setBrokerClient(BrokerClient client) {

    }
}
