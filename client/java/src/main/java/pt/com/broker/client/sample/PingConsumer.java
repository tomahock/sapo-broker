package pt.com.broker.client.sample;

import pt.com.broker.client.BrokerClient;
import pt.com.broker.types.NetPong;
import pt.com.broker.types.NetProtocolType;

public class PingConsumer
{

	public static void main(String[] args) throws Throwable
	{
		BrokerClient bk = new BrokerClient("192.168.100.1", 3323,"pt.com.broker.client.sample.PingConsumer", NetProtocolType.JSON);



        System.out.println("teste1");
		NetPong pong = bk.checkStatus();
        System.out.println("teste2");

		System.out.println(pong);
		System.out.println(pong.getActionId());

       // bk.close();

        /*Thread[] openThreads = new Thread[Thread.activeCount()];
        Thread.enumerate(openThreads);

        while (true){


            System.out.println("Total: "+Thread.activeCount());


            Thread.sleep(1000);


            for(Thread t : openThreads){
                System.out.println("Name: " +t.getName());
            }
        }*/



	}
}