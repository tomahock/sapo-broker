package pt.com.broker.client.nio.consumer;

import io.netty.channel.Channel;
import org.caudexorigo.text.StringUtils;
import pt.com.broker.client.nio.events.BrokerListener;
import pt.com.broker.client.nio.events.MessageAcceptedAdapter;;
import pt.com.broker.client.nio.types.ActionIdDecorator;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by luissantos on 09-05-2014.
 */
public class PendingAcceptRequestsManager {


    private final Map<String, BrokerListener> requests = new HashMap<String, BrokerListener>();

    private final ScheduledExecutorService eventLoopGroup;

    private final Map<String, ScheduledFuture> schedules = new HashMap<String, ScheduledFuture>();



    public PendingAcceptRequestsManager(ScheduledExecutorService eventLoopGroup) {
        this.eventLoopGroup = eventLoopGroup;
    }

    public void addAcceptRequest(final String actionID, long timeout , BrokerListener listener) throws Throwable{

        if(StringUtils.isEmpty(actionID)){
            throw new Exception("Invalid actionID");
        }

        synchronized (requests){

            if(requests.containsKey(actionID)){
                throw new Exception("ActionID already registered");
            }

            requests.put(actionID, listener);

            Runnable command = new Runnable(){

                @Override
                public void run() {

                    synchronized (requests) {

                        MessageAcceptedAdapter _listener = (MessageAcceptedAdapter) getListener(actionID);

                        removeAcceptRequest(actionID);

                        _listener.onTimeout(actionID);

                    }

                }
            };

            ScheduledFuture f = this.eventLoopGroup.schedule(command,timeout, TimeUnit.MILLISECONDS);

            schedules.put(actionID, f);

        }

    }


    public MessageAcceptedAdapter removeAcceptRequest(String actionID){

        MessageAcceptedAdapter b = (MessageAcceptedAdapter) requests.remove(actionID);

        if(b!=null){
            cancelTimeout(actionID);
        }


        return b;
    }


    protected BrokerListener getListener(String actionID){

        return requests.get(actionID);

    }

    protected boolean cancelTimeout(String actionID){

        boolean cancel_return = false;

        synchronized (schedules){

            Future f = schedules.get(actionID);

            if(f!=null){
                cancel_return = f.cancel(false);
            }

            schedules.remove(actionID);
        }

        return cancel_return;
    }


    public void deliverMessage(NetMessage netMessage, Channel channel) throws Exception {

        ActionIdDecorator decorator = new ActionIdDecorator(netMessage);

        String actionID = decorator.getActionId();

        if(StringUtils.isEmpty(actionID)){

            throw new Exception("Invalid actionID");

        }

        synchronized (requests) {

            BrokerListener listener = getListener(actionID);

            if (listener == null) {

                throw new Exception("No listener was found actionID: " + actionID);

            }


            try {

                listener.deliverMessage(netMessage,channel);

            } catch (Throwable throwable) {

                throwable.printStackTrace();

            }


            removeAcceptRequest(actionID);

        }


    }

}
