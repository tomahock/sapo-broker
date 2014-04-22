package pt.com.broker.client.nio.events;

import pt.com.broker.client.nio.BrokerClient;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetNotification;

/**
 * Created by luissantos on 22-04-2014.
 */
abstract  public class BrokerListenerAdapter implements BrokerListener {


    private BrokerClient brokerClient = null;


    public void preOnMessage(NetNotification netNotification){


    }

    public void postOnMessage(NetNotification netNotification) throws Throwable {

        if(getBrokerClient() instanceof  BrokerClient){
            this.ackMessage(netNotification);
        }

    }

    public BrokerClient getBrokerClient() {
        return brokerClient;
    }

    public void setBrokerClient(BrokerClient brokerClient) {
        this.brokerClient = brokerClient;
    }

    protected void ackMessage(NetNotification netNotification) throws Throwable {

        this.getBrokerClient().acknowledge(netNotification);

    }

    public final void deliverMessage(NetNotification netNotification) throws Throwable {

        preOnMessage(netNotification);
        onMessage(netNotification);
        postOnMessage(netNotification);
    }
}
