package pt.com.broker.monitorization.collectors;

import pt.com.broker.client.HostInfo;
import pt.com.broker.types.NetAction.DestinationType;

public class Test
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		try
		{
			HostInfo hostInfo = new HostInfo("localhost", 3323);
			SubscriptionCountCollector scc = new SubscriptionCountCollector( hostInfo );
			
			scc.addListener(new SubscriptionCountListener(){

				@Override
				public void onUpdate(String agentName, DestinationType subscriptionType, String subscriptionName, int count)
				{
					String s = String.format("SubscriptionCountListener -- Agent: %s, Destination type: %s, Subscription name: %s, Count: %s", agentName, subscriptionType.toString(),subscriptionName, count+"");
					System.out.println(s);
					
				}
				
			});
			
			scc.start();
			
			
			QueueSizeCollector qsc = new QueueSizeCollector( hostInfo );
			
			qsc.addListener( new QueueSizeListener(){

				@Override
				public void onUpdate(String agentName, String queueName, int size)
				{
					String s = String.format("QueueSizeListener -- Agent: %s, Queue name: %s, Size: %s", agentName, queueName, size+"");
					System.out.println(s);					
				}
				
			});
			
			qsc.start();
			
			System.in.read();
			System.out.println("Ending...");
			scc.stop();
			qsc.stop();
		}
		catch (Throwable e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}

}
