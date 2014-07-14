package pt.com.broker.client.nio;

import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;

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





    protected void fail(String msg){

    }


}
