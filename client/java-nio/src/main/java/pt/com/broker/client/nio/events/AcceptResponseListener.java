package pt.com.broker.client.nio.events;

import io.netty.channel.Channel;
import pt.com.broker.client.nio.server.HostInfo;
import pt.com.broker.client.nio.utils.ChannelDecorator;
import pt.com.broker.types.NetAccepted;
import pt.com.broker.types.NetFault;
import pt.com.broker.types.NetMessage;

/**
 * Created by luissantos on 30-05-2014.
 */
public abstract class AcceptResponseListener implements BrokerListener {

    @Override
    public final void deliverMessage(NetMessage message, HostInfo host) throws Throwable {


        NetAccepted accepted = message.getAction().getAcceptedMessage();


        if(accepted!=null){
            onMessage(accepted,host);
            return;
        }


        NetFault fault = message.getAction().getFaultMessage();

        if(fault!=null){
            onFault(fault,host);
            return;
        }


        throw new RuntimeException("Invalid message");

    }


    abstract public void onMessage(NetAccepted message, HostInfo host);

    abstract public void onFault(NetFault fault, HostInfo host);

    abstract public void onTimeout(String actionID);

}
