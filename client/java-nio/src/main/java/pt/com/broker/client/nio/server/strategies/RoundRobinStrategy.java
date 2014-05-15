package pt.com.broker.client.nio.server.strategies;

import pt.com.broker.client.nio.HostInfo;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Created by luissantos on 15-05-2014.
 */
public class RoundRobinStrategy implements SelectServerStrategy {


    Collection<HostInfo> hosts;

    Iterator iterator = null;

    @Override
    public void setCollection(Collection<HostInfo> servers) {
        this.hosts = servers;
    }

    @Override
    public HostInfo next() {

        synchronized (hosts) {

            if (iterator!= null && iterator.hasNext()){

                return (HostInfo) iterator.next();

            }else{

                iterator = hosts.iterator();

                if(iterator.hasNext()){
                    return (HostInfo) iterator.next();
                }

                return null;
            }
        }

    }
}
