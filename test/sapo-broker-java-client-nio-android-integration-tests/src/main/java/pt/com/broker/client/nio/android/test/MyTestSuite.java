package pt.com.broker.client.nio.android.test;



import com.uphyca.testing.AndroidJUnit4TestAdapter;
import com.uphyca.testing.AndroidTestCase;
import junit.framework.TestSuite;
import pt.com.broker.client.nio.consumer.AcceptRequestsTest;
import pt.com.broker.client.nio.consumer.ConsumerManager;
import pt.com.broker.client.nio.consumer.ConsumerManagerTest;
import pt.com.broker.client.nio.consumer.SubscriberManagerTest;
import pt.com.broker.client.nio.listener.TestAcceptResponseListener;
import pt.com.broker.client.nio.listener.TestErrorListener;
import pt.com.broker.client.nio.listener.TestNotificationListener;
import pt.com.broker.client.nio.listener.TestPongListener;
import pt.com.broker.client.nio.server.TestSelectServerStrategy;
import pt.com.broker.client.nio.server.TestServerConnection;
import pt.com.broker.client.nio.server.TestServerReconnect;


public class MyTestSuite extends AndroidTestCase{


    public static junit.framework.Test suite() {
        // Should use AndroidJUnit4TestAdapter for to running AndroidDependent
        // TestCases.

        TestSuite suite = new TestSuite();


        addServerTests(suite);

        addListenerTests(suite);


        addConsumerTests(suite);

        return suite;
    }


    public  static void addConsumerTests(TestSuite suite){

        addTest(suite,AcceptRequestsTest.class);
        addTest(suite,ConsumerManagerTest.class);
        addTest(suite,SubscriberManagerTest.class);
    }


    public  static void addListenerTests(TestSuite suite){

        addTest(suite,TestPongListener.class);
        addTest(suite,TestAcceptResponseListener.class);
        addTest(suite,TestErrorListener.class);
        addTest(suite,TestNotificationListener.class);

    }

    public static void addServerTests(TestSuite suite){
        addTest(suite, TestSelectServerStrategy.class);
        addTest(suite, TestServerConnection.class);
        addTest(suite, TestServerReconnect.class);

    }


    protected static void addTest(TestSuite suite, Class classobj){
        suite.addTest(new AndroidJUnit4TestAdapter(classobj));
    }




}
