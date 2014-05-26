package pt.com.broker.client.nio.server;

import pt.com.broker.client.nio.HostInfo;

/**
 * Created by luissantos on 22-05-2014.
 */
public class ReconnectEvent {

    protected HostInfo host;


    public ReconnectEvent(HostInfo host) {
        this.host = host;
    }

    public HostInfo getHost() {
        return host;
    }
}