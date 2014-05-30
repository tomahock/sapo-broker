package pt.com.broker.client.nio.server;


import junit.framework.Assert;
import org.junit.Test;
import pt.com.broker.client.nio.BaseTest;
import pt.com.broker.client.nio.server.strategies.RoundRobinStrategy;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by luissantos on 16-05-2014.
 */
public class TestSelectServerStrategy extends BaseTest{



    @Test()
    public void RoundRobinLoop(){


        List<HostInfo> list = new ArrayList<HostInfo>();

        list.add(new HostInfo("127.0.0.1",1234));
        list.add(new HostInfo("127.0.0.1",1234));
        list.add(new HostInfo("127.0.0.1",1234));
        list.add(new HostInfo("127.0.0.1",1234));
        list.add(new HostInfo("127.0.0.1",1234));
        list.add(new HostInfo("127.0.0.1",1234));

        RoundRobinStrategy strategy = new RoundRobinStrategy(list);

        for(HostInfo host : list){

            Assert.assertSame(host, strategy.next());

        }

        for(HostInfo host : list){

            Assert.assertSame(host, strategy.next());

        }


    }



    public void testRoundRobinLoopWithRemove(){


        List<HostInfo> list = new ArrayList<HostInfo>();

        list.add(new HostInfo("127.0.0.1",1234));
        list.add(new HostInfo("127.0.0.1",1234));
        list.add(new HostInfo("127.0.0.1",1234));
        list.add(new HostInfo("127.0.0.1",1234));
        list.add(new HostInfo("127.0.0.1",1234));
        list.add(new HostInfo("127.0.0.1",1234));

        RoundRobinStrategy strategy = new RoundRobinStrategy(list);

        for(HostInfo host : list){

            Assert.assertSame(host, strategy.next());

        }

        while (list.size()>0){

            HostInfo host = strategy.next();

            HostInfo removed = list.remove(list.size()-1);

            Assert.assertNotNull(host);

        }

    }



    public void testRoundRobinLoopWithRemoveAndAdd(){


        List<HostInfo> list = new ArrayList<HostInfo>();

        list.add(new HostInfo("127.0.0.1",1234));
        list.add(new HostInfo("127.0.0.1",1234));
        list.add(new HostInfo("127.0.0.1",1234));
        list.add(new HostInfo("127.0.0.1",1234));
        list.add(new HostInfo("127.0.0.1",1234));
        list.add(new HostInfo("127.0.0.1",1234));

        RoundRobinStrategy strategy = new RoundRobinStrategy(list);

        for(HostInfo host : list){

            Assert.assertSame(host, strategy.next());

        }

        while (list.size()>0){

            HostInfo host = strategy.next();

            list.remove(0);

            Assert.assertNotNull(host);

        }

        HostInfo host = strategy.next();

        Assert.assertNull(host);


        list.add(new HostInfo("127.0.0.1",1234));
        list.add(new HostInfo("127.0.0.1",1234));
        list.add(new HostInfo("127.0.0.1",1234));
        list.add(new HostInfo("127.0.0.1",1234));
        list.add(new HostInfo("127.0.0.1",1234));
        list.add(new HostInfo("127.0.0.1",1234));


        for(HostInfo host2 : list){

            Assert.assertSame(host2, strategy.next());

        }



    }


}
