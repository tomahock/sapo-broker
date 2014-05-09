package pt.com.broker.client.nio.events;

import pt.com.broker.types.NetMessage;

/**
 * Created by luissantos on 09-05-2014.
 */
abstract public class MessageAcceptedAdapter extends BrokerListenerAdapter implements MessageAcceptedListener {


    abstract public void onMessage(NetMessage message);

    abstract public void onFault(NetMessage message);

    abstract public void onTimeout(String actionID);

}
