package pt.com.broker.client.nio.events;

import io.netty.channel.Channel;
import pt.com.broker.client.nio.BrokerClient;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetNotification;

/**
 * Created by luissantos on 22-04-2014.
 */
abstract  public class BrokerListenerAdapter implements BrokerListener {


    private BrokerClient brokerClient = null;


    public void preOnMessage(NetMessage message,Channel channel){


    }

    public void postOnMessage(NetMessage message,Channel channel) throws Throwable {

        if(getBrokerClient() instanceof  BrokerClient){

            NetNotification netNotification = message.getAction().getNotificationMessage();

            if(netNotification!=null){

               this.ackMessage(message.getAction().getNotificationMessage(),channel);
            }

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

    protected boolean isFault(NetMessage netMessage){
        return netMessage.getAction().getActionType() == NetAction.ActionType.FAULT;
    }

    public final void deliverMessage(NetMessage netMessage,Channel channel) throws Throwable {


        preOnMessage(netMessage,channel);
        onMessage(netMessage);
        postOnMessage(netMessage,channel);

    }

    public abstract void onMessage(NetMessage message);
}
