package pt.com.broker.client.nio;

import junit.framework.Test;
import junit.framework.TestCase;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;
import pt.com.broker.client.nio.iptables.IpTables;
import pt.com.broker.client.nio.mocks.ServerFactory;
import pt.com.broker.client.nio.mocks.SocketServer;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by luissantos on 22-05-2014.
 */
public abstract class BaseTest{



    protected boolean skipTest(boolean condition){


        if(condition){

            StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
            StackTraceElement e = stacktrace[3];//maybe this number needs to be corrected
            String methodName = e.getMethodName();
            String className = e.getClassName();

           System.out.println(" Skiping Test "+ className +"#"+methodName);


        }

        Assume.assumeTrue(!condition);

        return condition;
    }

    protected void setUp() throws Exception {

    }

    protected void tearDown() throws Exception {

    }



    protected void fail(String msg){

    }


}
