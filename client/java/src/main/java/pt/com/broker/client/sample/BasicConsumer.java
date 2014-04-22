/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.com.broker.client.sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.com.broker.client.BrokerClient;
import pt.com.broker.client.messaging.BrokerListener;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetNotification;
import pt.com.broker.types.NetSubscribe;

/**
 *
 * @author brsantos
 */
public class BasicConsumer implements BrokerListener
{

    private static final Logger log = LoggerFactory.getLogger(BasicConsumer.class);
    private final BrokerClient client;

    public BasicConsumer(BrokerClient client)
    {
        this.client = client;
    }

    public void consume() throws Throwable
    {
        client.addAsyncConsumer(new NetSubscribe("^((?!/system).)*$", NetAction.DestinationType.TOPIC), this);
        client.addAsyncConsumer(new NetSubscribe("^((?!/system).)*$", NetAction.DestinationType.QUEUE), this);
    }

    public static void main(String[] args)
    {
        try
        {
            BasicConsumer cons = new BasicConsumer(new BrokerClient("127.0.0.1", 3323));
            cons.consume();
        }
        catch (Throwable ex)
        {
            log.error(ex.getMessage(), ex);
        }
    }

    @Override
    public boolean isAutoAck()
    {
        return false;
    }

    @Override
    public void onMessage(NetNotification message)
    {
        try
        {
            log.info("HEADERS: " + message.getHeaders());
            log.info(String.format("DEST(%s): %s", message.getDestinationType(), message.getDestination()));
            log.info(String.format("MESSAGE: %s", message.getMessage()));
//            client.acknowledge(message);
        }
        catch (Throwable ex)
        {
            log.error(ex.getMessage(), ex);
        }
    }

}
