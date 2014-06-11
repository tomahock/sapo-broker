package pt.com.broker.client.nio.utils;

import io.netty.channel.Channel;
import pt.com.broker.client.nio.server.HostInfo;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetBrokerMessage;
import pt.com.broker.types.NetNotification;

import java.util.Map;

/**
 * Created by luissantos on 26-05-2014.
 *
 * @author vagrant
 * @version $Id: $Id
 */
public class NetNotificationDecorator extends NetNotification implements DecoratorInterface<NetNotification> {

    private final NetNotification instance;

    protected HostInfo host;



    /**
     * <p>Constructor for NetNotificationDecorator.</p>
     *
     * @param instance a {@link pt.com.broker.types.NetNotification} object.
     * @param host a {@link pt.com.broker.client.nio.server.HostInfo} object.
     */
    public NetNotificationDecorator(NetNotification instance, HostInfo host) {
        super(null,null,null,null);
        this.instance = instance;
        setHost(host);
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
     * <p>getChannel.</p>
     *
     * @return a {@link io.netty.channel.Channel} object.
     */
    public Channel getChannel() {
        return (host != null) ?  host.getChannel() : null;
    }


    /** {@inheritDoc} */
    @Override
    public String getDestination() {
        return instance.getDestination();
    }

    /** {@inheritDoc} */
    @Override
    public String getSubscription() {
        return instance.getSubscription();
    }

    /** {@inheritDoc} */
    @Override
    public NetAction.DestinationType getDestinationType() {
        return instance.getDestinationType();
    }

    /** {@inheritDoc} */
    @Override
    public NetBrokerMessage getMessage() {
        return instance.getMessage();
    }

    /** {@inheritDoc} */
    @Override
    public void setHeaders(Map<String, String> headers) {
        instance.setHeaders(headers);
    }

    /** {@inheritDoc} */
    @Override
    public Map<String, String> getHeaders() {
        return instance.getHeaders();
    }

    /**
     * <p>Getter for the field <code>instance</code>.</p>
     *
     * @return a {@link pt.com.broker.types.NetNotification} object.
     */
    public NetNotification getInstance() {
        return instance;
    }
}
