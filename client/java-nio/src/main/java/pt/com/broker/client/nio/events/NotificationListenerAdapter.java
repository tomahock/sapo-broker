package pt.com.broker.client.nio.events;

import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.com.broker.client.nio.BrokerClient;
import pt.com.broker.client.nio.server.HostInfo;
import pt.com.broker.client.nio.utils.ChannelDecorator;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetNotification;

/**
 * Created by luissantos on 26-05-2014.
 */
public abstract class NotificationListenerAdapter implements BrokerListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationListenerAdapter.class);

    BrokerClient brokerClient;

    @Override
    public final void deliverMessage(NetMessage message, HostInfo host) throws Throwable {

            NetAction netAction = message.getAction();

            NetNotification netNotification = netAction.getNotificationMessage();


            if(netNotification != null){

                if(onMessage(netNotification,host)){

                    if (brokerClient != null) {

                        // acknowledge if not a topic
                        if (netNotification.getDestinationType() != NetAction.DestinationType.TOPIC ) {

                            brokerClient.acknowledge(netNotification,host);
                        }


                    } else{

                        log.debug("Message not acknowledged because broker client was not set ");

                    }


                }

            }

    }


    public final void setBrokerClient(BrokerClient client) {
        brokerClient = client;
    }

    public abstract boolean onMessage(NetNotification notification, HostInfo host);


}
