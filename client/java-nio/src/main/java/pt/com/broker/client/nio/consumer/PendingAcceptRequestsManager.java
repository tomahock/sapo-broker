package pt.com.broker.client.nio.consumer;

import org.apache.commons.lang3.StringUtils;
import pt.com.broker.client.nio.events.AcceptResponseListener;
import pt.com.broker.client.nio.events.BrokerListener;
import pt.com.broker.client.nio.server.HostInfo;
import pt.com.broker.client.nio.types.ActionIdDecorator;
import pt.com.broker.types.NetMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by luissantos on 09-05-2014.
 *
 * @author vagrant
 * @version $Id: $Id
 */
public class PendingAcceptRequestsManager {


    private final Map<String, BrokerListener> requests = new HashMap<String, BrokerListener>();

    private final ScheduledExecutorService schedule = Executors.newScheduledThreadPool(2);

    private final Map<String, ScheduledFuture> schedules = new HashMap<String, ScheduledFuture>();



    /**
     * <p>Constructor for PendingAcceptRequestsManager.</p>
     */
    public PendingAcceptRequestsManager() {

    }

    /**
     * <p>addAcceptRequest.</p>
     *
     * @param actionID a {@link java.lang.String} object.
     * @param timeout a long.
     * @param listener a {@link pt.com.broker.client.nio.events.BrokerListener} object.
     * @throws java.lang.Throwable if any.
     */
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

                        AcceptResponseListener _listener = (AcceptResponseListener) getListener(actionID);

                        if(_listener!=null) {

                            removeAcceptRequest(actionID);

                            _listener.onTimeout(actionID);
                        }

                    }

                }
            };

            ScheduledFuture f = schedule.schedule(command,timeout, TimeUnit.MILLISECONDS);

            schedules.put(actionID, f);

        }

    }


    /**
     * <p>removeAcceptRequest.</p>
     *
     * @param actionID a {@link java.lang.String} object.
     * @return a {@link pt.com.broker.client.nio.events.AcceptResponseListener} object.
     */
    public AcceptResponseListener removeAcceptRequest(String actionID){

        AcceptResponseListener b = (AcceptResponseListener) requests.remove(actionID);

        if(b!=null){
            cancelTimeout(actionID);
        }


        return b;
    }


    /**
     * <p>getListener.</p>
     *
     * @param actionID a {@link java.lang.String} object.
     * @return a {@link pt.com.broker.client.nio.events.BrokerListener} object.
     */
    public BrokerListener getListener(String actionID){

        return requests.get(actionID);

    }

    /**
     * <p>cancelTimeout.</p>
     *
     * @param actionID a {@link java.lang.String} object.
     * @return a boolean.
     */
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


    /**
     * <p>deliverMessage.</p>
     *
     * @param netMessage a {@link pt.com.broker.types.NetMessage} object.
     * @param host a {@link pt.com.broker.client.nio.server.HostInfo} object.
     * @throws java.lang.Exception if any.
     */
    public void deliverMessage(NetMessage netMessage, HostInfo host) throws Exception {

        ActionIdDecorator decorator = new ActionIdDecorator(netMessage);

        String actionID = decorator.getActionId();

        if(StringUtils.isEmpty(actionID)){

            throw new Exception("Invalid actionID");

        }

        synchronized (requests) {

            BrokerListener listener = getListener(actionID);

            if (listener != null) {

                try {

                    listener.deliverMessage(netMessage,host);

                } catch (Throwable throwable) {

                    throwable.printStackTrace();

                }


                removeAcceptRequest(actionID);
            }else
            {
                //@todo log information
            }
        }


    }

}
