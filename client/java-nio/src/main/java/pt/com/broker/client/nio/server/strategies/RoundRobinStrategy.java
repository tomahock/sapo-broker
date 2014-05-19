package pt.com.broker.client.nio.server.strategies;

import pt.com.broker.client.nio.HostInfo;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by luissantos on 15-05-2014.
 */
public class RoundRobinStrategy implements SelectServerStrategy {


    private  List<HostInfo> hosts;

    private  int position = 0;

    public RoundRobinStrategy() {

    }

    public RoundRobinStrategy(List<HostInfo> hosts) {
        setCollection(hosts);
    }

    @Override
    public void setCollection(List<HostInfo> servers) {
        this.hosts = servers;
    }

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