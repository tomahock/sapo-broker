package pt.com.broker.client.nio;

import pt.com.broker.client.nio.events.AcceptResponseListener;
import pt.com.broker.client.nio.events.BrokerListener;

/**
 * Created by luissantos on 09-05-2014.
 *
 * @author vagrant
 * @version $Id: $Id
 */
public class AcceptRequest {

    private String actionId;
    private BrokerListener listner;
    private long timeout;

    /**
     * Creates an instance of AcceptRequest.
     *
     * @param actionId
     *            Message action identifier
     * @param listner
     *            An implementation of MessageAcceptedListener.
     * @param timeout
     *            A time interval, in milliseconds, during witch the Accept message is expected.
     */
    public AcceptRequest(String actionId, AcceptResponseListener listner, long timeout)
    {
        if (actionId == null)
            throw new IllegalArgumentException("actionId is null");
        if (listner == null)
            throw new IllegalArgumentException("listner is null");
        if (timeout <= 0)
            throw new IllegalArgumentException("timeout <= 0");

        this.actionId = actionId;
        this.listner = listner;
        this.timeout = timeout;
    }

    /**
     * <p>Getter for the field <code>actionId</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getActionId()
    {
        return actionId;
    }

    /**
     * <p>getListener.</p>
     *
     * @return a {@link pt.com.broker.client.nio.events.BrokerListener} object.
     */
    public BrokerListener getListener()
    {
        return listner;
    }

    /**
     * <p>Setter for the field <code>timeout</code>.</p>
     *
     * @param timeout a long.
     */
    public void setTimeout(long timeout)
    {
        this.timeout = timeout;
    }

    /**
     * <p>Getter for the field <code>timeout</code>.</p>
     *
     * @return a long.
     */
    public long getTimeout()
    {
        return timeout;
    }
}
