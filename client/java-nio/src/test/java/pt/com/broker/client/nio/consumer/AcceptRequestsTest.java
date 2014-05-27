package pt.com.broker.client.nio.consumer;


import org.junit.Assert;
import org.junit.Test;
import pt.com.broker.client.nio.HostInfo;
import pt.com.broker.client.nio.events.BrokerListener;
import pt.com.broker.client.nio.events.MessageAcceptedAdapter;
import pt.com.broker.client.nio.types.ActionIdDecorator;
import pt.com.broker.types.NetAccepted;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetFault;
import pt.com.broker.types.NetMessage;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by luissantos on 12-05-2014.
 */
public class AcceptRequestsTest {



    @Test()
    public void addRemove(){

        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);

        PendingAcceptRequestsManager manager = new PendingAcceptRequestsManager(scheduledExecutorService);

        String actionID = UUID.randomUUID().toString();

        MessageAcceptedAdapter acceptedListener = new MessageAcceptedAdapter() {

            @Override
            public void onMessage(NetAccepted message, HostInfo host) {

            }

            @Override
            public void onFault(NetFault fault, HostInfo host) {

            }

            @Override
            public void onTimeout(String actionID) {

            }
        };


        try {

            manager.addAcceptRequest(actionID,1000,acceptedListener);

        } catch (Throwable throwable) {

            throwable.printStackTrace();

        }

        BrokerListener listener1 = manager.getListener(actionID);

        Assert.assertSame(acceptedListener,listener1);


        MessageAcceptedAdapter listener2 =  manager.removeAcceptRequest(actionID);

        Assert.assertNotNull(listener2);



        BrokerListener listener3 = manager.getListener(actionID);

        Assert.assertNull(listener3);


    }

    @Test()
    public void testTimeout() throws InterruptedException {

        long timeout = 2000L;

        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);

        PendingAcceptRequestsManager manager = new PendingAcceptRequestsManager(scheduledExecutorService);

        final String actionID = UUID.randomUUID().toString();

        final AtomicBoolean isTimeout = new AtomicBoolean(false);


        MessageAcceptedAdapter acceptedListener = new MessageAcceptedAdapter() {


            @Override
            public void onMessage(NetAccepted message, HostInfo host) {

            }

            @Override
            public void onFault(NetFault fault, HostInfo host) {

            }

            @Override
            public void onTimeout(String _actionID) {

                if(actionID.equals(_actionID)){
                    isTimeout.set(true);
                }

            }

        };


        try {

            manager.addAcceptRequest(actionID,timeout,acceptedListener);

        } catch (Throwable throwable) {

            throwable.printStackTrace();

        }

        scheduledExecutorService.shutdown();
        scheduledExecutorService.awaitTermination(timeout*2, TimeUnit.MILLISECONDS);

        Assert.assertTrue("Timeout failed",isTimeout.get());

    }


    @Test()
    public void testDeliver() throws Exception {


        long timeout = 2000L;

        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);

        PendingAcceptRequestsManager manager = new PendingAcceptRequestsManager(scheduledExecutorService);

        final String actionID = UUID.randomUUID().toString();

        final AtomicBoolean onMessage = new AtomicBoolean(false);


        MessageAcceptedAdapter acceptedListener = new MessageAcceptedAdapter() {


            @Override
            public void onMessage(NetAccepted message, HostInfo host) {

                onMessage.set(true);
            }

            @Override
            public void onFault(NetFault fault, HostInfo host) {


            }

            @Override
            public void onTimeout(String actionID) {


            }
        };


        try {

            manager.addAcceptRequest(actionID,timeout,acceptedListener);

        } catch (Throwable throwable) {

            throwable.printStackTrace();

        }


        NetAccepted netAccepted = new NetAccepted(actionID);

        NetAction netAction = new NetAction(NetAction.ActionType.ACCEPTED);

        netAction.setAcceptedMessage(netAccepted);

        NetMessage netMessage = new NetMessage(netAction);


        manager.deliverMessage(netMessage,null);

        Assert.assertTrue(onMessage.get());

    }
}
