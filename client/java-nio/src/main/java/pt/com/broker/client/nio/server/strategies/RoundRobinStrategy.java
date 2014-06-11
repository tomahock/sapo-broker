package pt.com.broker.client.nio.server.strategies;

import pt.com.broker.client.nio.server.HostInfo;

import java.util.List;

/**
 * Created by luissantos on 15-05-2014.
 *
 * @author vagrant
 * @version $Id: $Id
 */
public class RoundRobinStrategy implements SelectServerStrategy {


    private  List<HostInfo> hosts;

    private  int position = 0;

    /**
     * <p>Constructor for RoundRobinStrategy.</p>
     */
    public RoundRobinStrategy() {

    }

    /**
     * <p>Constructor for RoundRobinStrategy.</p>
     *
     * @param hosts a {@link java.util.List} object.
     */
    public RoundRobinStrategy(List<HostInfo> hosts) {
        setCollection(hosts);
    }

    /** {@inheritDoc} */
    @Override
    public void setCollection(List<HostInfo> servers) {
        this.hosts = servers;
    }

    /** {@inheritDoc} */
    @Override
    public HostInfo next() {


        synchronized (hosts) {

            int size = hosts.size();

            while (position < size){

                HostInfo host = hosts.get(position);

                position ++;

                return host;
            }

            position = 0;

            if(size == 0){
                return null;
            }


            return next();

        }

    }
}
