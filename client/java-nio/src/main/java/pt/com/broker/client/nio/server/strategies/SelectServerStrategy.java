package pt.com.broker.client.nio.server.strategies;

import pt.com.broker.client.nio.HostInfo;

import java.util.Collection;

/**
 * Created by luissantos on 15-05-2014.
 */
public interface SelectServerStrategy {

    public void setCollection(Collection<HostInfo> servers);

    public HostInfo next();

}
