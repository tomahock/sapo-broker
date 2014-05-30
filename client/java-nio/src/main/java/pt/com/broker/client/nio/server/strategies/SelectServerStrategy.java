package pt.com.broker.client.nio.server.strategies;

import pt.com.broker.client.nio.server.HostInfo;

import java.util.List;

/**
 * Created by luissantos on 15-05-2014.
 */
public interface SelectServerStrategy {

    public void setCollection(List<HostInfo> servers);

    public HostInfo next();

}
