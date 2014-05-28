package pt.com.broker.client.nio;

import junit.framework.TestCase;
import org.junit.AfterClass;
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
public abstract class BaseTest extends TestCase {


    protected boolean isAndroid(){
        return "Dalvik".equals(System.getProperty("java.vm.name"));
    }

    protected boolean isUnix() {
        String OS = System.getProperty("os.name").toLowerCase();
        return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 );
    }

    protected boolean isLinux() {
        return System.getProperty("os.name").toLowerCase().equals("linux");
    }

    protected Integer getPlatformVersion() {

        try {

            Field verField = Class.forName("android.os.Build$VERSION").getField("SDK_INT");
            int ver = verField.getInt(verField);
            return ver;

        } catch (Exception e) {

            try {

                Field verField = Class.forName("android.os.Build$VERSION").getField("SDK");
                String verString = (String) verField.get(verField);
                return Integer.parseInt(verString);

            } catch(Exception e2) {
                return null;
            }

        }

    }

    protected boolean skipTest(boolean condition){

        if(condition){

            StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
            StackTraceElement e = stacktrace[2];//maybe this number needs to be corrected
            String methodName = e.getMethodName();
            String className = e.getClassName();

           System.out.println(" Skiping Test "+ className +"#"+methodName);
        }

        return condition;
    }


}
