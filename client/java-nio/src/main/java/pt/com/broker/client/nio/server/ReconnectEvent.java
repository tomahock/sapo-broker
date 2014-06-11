package pt.com.broker.client.nio.server;

/**
 * Created by luissantos on 22-05-2014.
 *
 * @author vagrant
 * @version $Id: $Id
 */
public class ReconnectEvent {

    protected HostInfo host;


    /**
     * <p>Constructor for ReconnectEvent.</p>
     *
     * @param host a {@link pt.com.broker.client.nio.server.HostInfo} object.
     */
    public ReconnectEvent(HostInfo host) {
        this.host = host;
    }

    /**
     * <p>Getter for the field <code>host</code>.</p>
     *
     * @return a {@link pt.com.broker.client.nio.server.HostInfo} object.
     */
    public HostInfo getHost() {
        return host;
    }
}
