package pt.com.broker.client.nio.events;

import io.netty.channel.Channel;
import pt.com.broker.client.nio.BrokerClient;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetNotification;

/**
 * Created by luissantos on 26-05-2014.
 */
public abstract class NotificationListenerAdapter implements BrokerListener {

    BrokerClient brokerClient = null;

    @Override
    public void deliverMessage(NetMessage message, Channel channel) throws Throwable {

        if(message!=null){

            NetAction netAction = message.getAction();

            if(netAction.getActionType() == NetAction.ActionType.NOTIFICATION){

                NetNotification netNotification = netAction.getNotificationMessage();

                if(onMessage(netNotification)){

                    if(brokerClient != null){

                        // acknowledge if not a topic
                        if(netNotification.getDestinationType().equals(NetAction.DestinationType.TOPIC)){

                            brokerClient.acknowledge(netNotification);
                        }


                    }


                }

            }

        }


    }

    @Override
    public void setBrokerClient(BrokerClient client) {

        brokerClient = client;

    }


    public abstract boolean onMessage(NetNotification notification);



}
