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
        if(isFault(netMessage)){
            onFault(netMessage);
        }else{
            if(onMessage(netMessage)){

                if(getBrokerClient() instanceof  BrokerClient){

                    NetNotification netNotification = netMessage.getAction().getNotificationMessage();

                    if(netNotification!=null){

                        this.ackMessage(netMessage.getAction().getNotificationMessage(),channel);
                    }

                }


            };
        }
        postOnMessage(netMessage,channel);

    }

    public abstract boolean onMessage(NetMessage message);

    public void onFault(NetMessage message){

    }
}
