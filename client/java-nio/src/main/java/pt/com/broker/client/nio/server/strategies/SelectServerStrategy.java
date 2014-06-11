package pt.com.broker.client.nio.server.strategies;

import pt.com.broker.client.nio.server.HostInfo;

import java.util.List;

/**
 * Created by luissantos on 15-05-2014.
 *
 * @author vagrant
 * @version $Id: $Id
 */
public interface SelectServerStrategy {

    /**
     * <p>setCollection.</p>
     *
     * @param servers a {@link java.util.List} object.
     */
    public void setCollection(List<HostInfo> servers);

    /**
     * <p>next.</p>
     *
     * @return a {@link pt.com.broker.client.nio.server.HostInfo} object.
     */
    public HostInfo next();

}
