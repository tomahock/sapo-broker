package pt.com.broker.client.nio.consumer;


import org.junit.Assert;
import org.junit.Test;

import pt.com.broker.client.nio.BaseTest;
import pt.com.broker.client.nio.events.AcceptResponseListener;
import pt.com.broker.client.nio.events.BrokerListener;
import pt.com.broker.client.nio.server.HostInfo;
import pt.com.broker.types.NetAccepted;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetFault;
import pt.com.broker.types.NetMessage;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by luissantos on 12-05-2014.
 */
public class AcceptRequestsTest extends BaseTest {

    @Test()
    public void testAddRemove() throws Throwable{
        PendingAcceptRequestsManager manager = new PendingAcceptRequestsManager();
        String actionID = UUID.randomUUID().toString();
        AcceptResponseListener acceptedListener = new AcceptResponseListener() {

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
        manager.addAcceptRequest(actionID,1000,acceptedListener);
        BrokerListener listener1 = manager.getListener(actionID);
        Assert.assertSame(acceptedListener, listener1);
        AcceptResponseListener listener2 =  manager.removeAcceptRequest(actionID);
        Assert.assertNotNull(listener2);
        BrokerListener listener3 = manager.getListener(actionID);
        Assert.assertNull(listener3);
    }


    @Test()
    public void testTimeout() throws Throwable {
        long timeout = 2000L;
//        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);
        PendingAcceptRequestsManager manager = new PendingAcceptRequestsManager();
        final String actionID = UUID.randomUUID().toString();
        final AtomicBoolean isTimeout = new AtomicBoolean(false);
        AcceptResponseListener acceptedListener = new AcceptResponseListener() {

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
        manager.addAcceptRequest(actionID,timeout,acceptedListener);
        Thread.sleep(timeout+1000);
        Assert.assertTrue("Timeout failed",isTimeout.get());
    }

    @Test()
    public void testDeliver() throws Throwable {
        long timeout = 2000L;
//        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);
        PendingAcceptRequestsManager manager = new PendingAcceptRequestsManager();
        final String actionID = UUID.randomUUID().toString();
        final AtomicBoolean onMessage = new AtomicBoolean(false);
        AcceptResponseListener acceptedListener = new AcceptResponseListener() {

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
        manager.addAcceptRequest(actionID,timeout,acceptedListener);
        NetAccepted netAccepted = new NetAccepted(actionID);
        NetAction netAction = new NetAction(NetAction.ActionType.ACCEPTED);
        netAction.setAcceptedMessage(netAccepted);
        NetMessage netMessage = new NetMessage(netAction);
        HostInfo host = new HostInfo("127.0.0.1",3323);
        manager.deliverMessage(netMessage, host);
        Assert.assertTrue(onMessage.get());
    }
}
