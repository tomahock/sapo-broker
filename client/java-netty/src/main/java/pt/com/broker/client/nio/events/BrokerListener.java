package pt.com.broker.client.nio.events;

import pt.com.broker.types.NetNotification;

/**
 * Created by luissantos on 21-04-2014.
 */
public interface BrokerListener extends BrokerEventHandler {

    /**
    * Fired when a message arrives.
    *
    * @param message The message.
    */
    void onMessage(NetNotification message);




}
