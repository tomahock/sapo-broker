package pt.com.broker.client.nio.consumer;

import io.netty.channel.EventLoopGroup;
import org.caudexorigo.text.StringUtils;
import pt.com.broker.client.nio.events.BrokerListener;
import pt.com.broker.client.nio.events.MessageAcceptedListener;
import pt.com.broker.client.nio.types.ActionIdDecorator;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by luissantos on 09-05-2014.
 */
public class PendingAcceptRequestsManager {


    private final Map<String, MessageAcceptedListener> requests = new HashMap<String, MessageAcceptedListener>();

    private final EventLoopGroup eventLoopGroup;

    private final Map<String, ScheduledFuture> schedules = new HashMap<String, ScheduledFuture>();



    public PendingAcceptRequestsManager(EventLoopGroup eventLoopGroup) {
        this.eventLoopGroup = eventLoopGroup;
    }

    public void addAcceptRequest(final String actionID, long timeout , MessageAcceptedListener listener) throws Throwable{

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

                        MessageAcceptedListener _listener = getListner(actionID);

                        removeAcceptRequest(actionID);

                        _listener.onTimeout(actionID);

                    }

                }
            };

            ScheduledFuture f = this.eventLoopGroup.schedule(command,timeout, TimeUnit.MILLISECONDS);

            schedules.put(actionID, f);

        }

    }


    public MessageAcceptedListener removeAcceptRequest(String actionID){



        MessageAcceptedListener b =  requests.remove(actionID);

        if(b!=null){
            cancelTimeout(actionID);
        }


        return b;
    }


    protected MessageAcceptedListener getListner(String actionID){

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


    public void deliverMessage(NetMessage netMessage) throws Exception {

        ActionIdDecorator decorator = new ActionIdDecorator(netMessage);

        String actionID = decorator.getActiondId();

        if(StringUtils.isEmpty(actionID)){
            throw new Exception("Invalid actionID");
        }

        synchronized (requests) {

            MessageAcceptedListener listener = getListner(actionID);

            if (listener == null) {
                throw new Exception("No listener was found actionID: " + actionID);
            }



            if (netMessage.getAction().getActionType() == NetAction.ActionType.FAULT) {

                listener.onFault(netMessage);

            }else{

                listener.onMessage(netMessage);

            }

            removeAcceptRequest(actionID);

        }
    }

}
