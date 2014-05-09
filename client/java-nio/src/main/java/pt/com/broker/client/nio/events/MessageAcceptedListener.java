package pt.com.broker.client.nio.events;

import pt.com.broker.types.NetMessage;

/**
 * Created by luissantos on 09-05-2014.
 */
public interface MessageAcceptedListener {

    public void onMessage(NetMessage message);

    public void onFault(NetMessage message);

    public void onTimeout(String actionID);


}
