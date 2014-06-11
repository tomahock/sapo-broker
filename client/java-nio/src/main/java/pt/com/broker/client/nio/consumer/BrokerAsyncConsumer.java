package pt.com.broker.client.nio.consumer;

import io.netty.channel.Channel;
import pt.com.broker.client.nio.server.HostInfo;
import pt.com.broker.client.nio.events.BrokerListener;
import pt.com.broker.types.*;

/**
 * Created by luissantos on 22-04-2014.
 *
 * @author vagrant
 * @version $Id: $Id
 */
public class BrokerAsyncConsumer {


    private final NetAction.DestinationType destinationType;

    private final String destinationName;

    private final BrokerListener listener;

    private HostInfo host;


    /**
     * <p>Constructor for BrokerAsyncConsumer.</p>
     *
     * @param destinationName a {@link java.lang.String} object.
     * @param destinationType a {@link pt.com.broker.types.NetAction.DestinationType} object.
     * @param listener a {@link pt.com.broker.client.nio.events.BrokerListener} object.
     */
    public BrokerAsyncConsumer(String destinationName, NetAction.DestinationType destinationType,  BrokerListener listener)
    {

        this.destinationType = destinationType;
        this.listener = listener;
        this.destinationName = destinationName;



    }

    /**
     * <p>deliver.</p>
     *
     * @param msg a {@link pt.com.broker.types.NetMessage} object.
     * @param host a {@link pt.com.broker.client.nio.server.HostInfo} object.
     * @return a boolean.
     * @throws java.lang.Throwable if any.
     */
    public boolean deliver(NetMessage msg,HostInfo host) throws Throwable {

        listener.deliverMessage(msg,host);

        return true;
    }

    /**
     * <p>Getter for the field <code>destinationType</code>.</p>
     *
     * @return a {@link pt.com.broker.types.NetAction.DestinationType} object.
     */
    public NetAction.DestinationType getDestinationType() {
        return destinationType;
    }

    /**
     * <p>Getter for the field <code>destinationName</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDestinationName() {
        return destinationName;
    }


    /**
     * <p>Getter for the field <code>host</code>.</p>
     *
     * @return a {@link pt.com.broker.client.nio.server.HostInfo} object.
     */
    public HostInfo getHost() {
        return host;
    }

    /**
     * <p>Setter for the field <code>host</code>.</p>
     *
     * @param host a {@link pt.com.broker.client.nio.server.HostInfo} object.
     */
    public void setHost(HostInfo host) {
        this.host = host;
    }

    /**
     * <p>Getter for the field <code>listener</code>.</p>
     *
     * @return a {@link pt.com.broker.client.nio.events.BrokerListener} object.
     */
    public BrokerListener getListener() {
        return listener;
    }
}
