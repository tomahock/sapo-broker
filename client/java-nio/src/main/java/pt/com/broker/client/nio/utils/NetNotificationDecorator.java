package pt.com.broker.client.nio.utils;

import io.netty.channel.Channel;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetBrokerMessage;
import pt.com.broker.types.NetNotification;

import java.util.Map;

/**
 * Created by luissantos on 26-05-2014.
 */
public class NetNotificationDecorator extends NetNotification {

    private final NetNotification instance;

    protected Channel channel;

    public NetNotificationDecorator(NetNotification instance) {
        this(instance, null);
    }


    public NetNotificationDecorator(NetNotification instance, Channel channel) {
        super(null,null,null,null);
        this.instance = instance;
        setChannel(channel);
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
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


}
