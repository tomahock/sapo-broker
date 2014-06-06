package pt.com.broker.client.nio.utils;

import io.netty.channel.Channel;
import pt.com.broker.client.nio.server.HostInfo;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetBrokerMessage;
import pt.com.broker.types.NetNotification;

import java.util.Map;

/**
 * Created by luissantos on 26-05-2014.
 */
public class NetNotificationDecorator extends NetNotification implements DecoratorInterface<NetNotification> {

    private final NetNotification instance;

    protected HostInfo host;



    public NetNotificationDecorator(NetNotification instance, HostInfo host) {
        super(null,null,null,null);
        this.instance = instance;
        setHost(host);
    }

    public HostInfo getHost() {
        return host;
    }

    public void setHost(HostInfo host) {
        this.host = host;
    }

    public Channel getChannel() {
        return (host != null) ?  host.getChannel() : null;
    }


    @Override
    public String getDestination() {
        return instance.getDestination();
    }

    @Override
    public String getSubscription() {
        return instance.getSubscription();
    }

    @Override
    public NetAction.DestinationType getDestinationType() {
        return instance.getDestinationType();
    }

    @Override
    public NetBrokerMessage getMessage() {
        return instance.getMessage();
    }

    @Override
    public void setHeaders(Map<String, String> headers) {
        instance.setHeaders(headers);
    }

    @Override
    public Map<String, String> getHeaders() {
        return instance.getHeaders();
    }

    public NetNotification getInstance() {
        return instance;
    }
}
