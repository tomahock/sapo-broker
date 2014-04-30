package pt.com.broker.client.nio.events;

import io.netty.channel.Channel;
import pt.com.broker.client.nio.BrokerClient;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetNotification;

/**
 * Created by luissantos on 22-04-2014.
 */
abstract  public class BrokerListenerAdapter implements BrokerListener {


    private BrokerClient brokerClient = null;


    public void preOnMessage(NetNotification netNotification,Channel channel){


    }

    public void postOnMessage(NetNotification netNotification,Channel channel) throws Throwable {

        if(getBrokerClient() instanceof  BrokerClient){
            this.ackMessage(netNotification,channel);
        }

    }

    public BrokerClient getBrokerClient() {
        return brokerClient;
    }

    public void setBrokerClient(BrokerClient brokerClient) {
        this.brokerClient = brokerClient;
    }

    protected void ackMessage(NetNotification netNotification,Channel channel) throws Throwable {

        this.getBrokerClient().acknowledge(netNotification,channel);

    }

    public final void deliverMessage(NetMessage netMessage,Channel channel) throws Throwable {

        NetNotification notification = netMessage.getAction().getNotificationMessage();

        preOnMessage(notification,channel);
        onMessage(notification);
        postOnMessage(notification,channel);

    }

    public abstract void onMessage(NetNotification message);
}
