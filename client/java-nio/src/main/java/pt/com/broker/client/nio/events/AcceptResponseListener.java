package pt.com.broker.client.nio.events;

import io.netty.channel.Channel;
import pt.com.broker.client.nio.server.HostInfo;
import pt.com.broker.client.nio.utils.ChannelDecorator;
import pt.com.broker.types.NetAccepted;
import pt.com.broker.types.NetFault;
import pt.com.broker.types.NetMessage;

/**
 * Created by luissantos on 30-05-2014.
 *
 * @author vagrant
 * @version $Id: $Id
 */
public abstract class AcceptResponseListener implements BrokerListener {

    /** {@inheritDoc} */
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


    /**
     * <p>onMessage.</p>
     *
     * @param message a {@link pt.com.broker.types.NetAccepted} object.
     * @param host a {@link pt.com.broker.client.nio.server.HostInfo} object.
     */
    abstract public void onMessage(NetAccepted message, HostInfo host);

    /**
     * <p>onFault.</p>
     *
     * @param fault a {@link pt.com.broker.types.NetFault} object.
     * @param host a {@link pt.com.broker.client.nio.server.HostInfo} object.
     */
    abstract public void onFault(NetFault fault, HostInfo host);

    /**
     * <p>onTimeout.</p>
     *
     * @param actionID a {@link java.lang.String} object.
     */
    abstract public void onTimeout(String actionID);

}
