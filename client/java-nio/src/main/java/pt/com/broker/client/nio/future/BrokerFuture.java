package pt.com.broker.client.nio.future;

import pt.com.broker.client.nio.BrokerClient;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by luissantos on 29-04-2014.
 */
public abstract class BrokerFuture implements Future {

    protected  BrokerClient bk;

    public BrokerFuture(BrokerClient bk){
        this.bk = bk;
    }


}
