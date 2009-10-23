package pt.com.broker.monitorization;

import java.util.Collection;

import pt.com.broker.monitorization.collectors.CollectorManager;
import pt.com.broker.monitorization.consolidator.ConsolidatorManager;
import pt.com.broker.monitorization.consolidator.db.DbFault;
import pt.com.broker.monitorization.consolidator.db.DbQueue;
import pt.com.broker.monitorization.consolidator.db.DbSubscription;
import pt.com.broker.monitorization.consolidator.db.DbDropbox;
import pt.com.broker.monitorization.consolidator.db.H2ConsolidatorManager;

public class Test
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		try
		{
			System.out.println("Sapo-Broker monitorization (almost)");
			System.out.println("Press X to exit.");
			
			CollectorManager.init("THIS SHOULD BE A FILE PATH");
			
			//ConsolidatorManager.init();
			H2ConsolidatorManager.init();

			int data;
			while (true)
			{
				System.out.println("Chose an option:");
				System.out.println("  [A] - List agents");
				System.out.println("  [B] - List queues for agent and message count");
				System.out.println("  [C] - List consolidated queue information");
				System.out.println("  [D] - List subscriptions for agent and subscription count");
				System.out.println("  [E] - List consolidated subscription information");
				System.out.println("  [F] - List dropboxes");
				System.out.println("  [G] - List faults");
				
				do{ data = System.in.read(); } while (data == 10);  
				if (data == -1)
					return;
				char option = (char) data;
				
				switch (option)
				{
				case 'A':
					showAgentList();
					break;
				case 'B':
					showQueuetList();
					break;
				case 'C':
					showConsolidatedQueuetList();
					break;
				case 'D':
					showSubscriptiontList();
					break;
				case 'E':
					showConsolidatedSubscriptionList();
					break;
				case 'F':
					showDropboxList();
					break;
				case 'G':
					showFaultsList();
					break;
				case 'X':
					System.out.println("Bye");
					ConsolidatorManager.stop();
					return;
					
				default:
					System.out.println("Invalid option");
					break;
				}
			}
		}
		catch (Throwable e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void showAgentList()
	{
		System.out.println("Agent List: NOT IMPLEMENTED");
	}
	
	private static void showQueuetList()
	{
		System.out.println("Queue List:");

		Collection<DbQueue> allQueueCount = DbQueue.getAllQueueCount();
		System.out.println("Total records: " + allQueueCount.size());
		for(DbQueue queue : allQueueCount)
		{
			System.out.printf("   * %s : %s : %s : %s  %n", queue.getAgentName(), queue.getName(), queue.getCount(), queue.getDate());
		}
		System.out.println();
	}

	private static void showConsolidatedQueuetList()
	{
		System.out.println("Queue List:");
		Collection<DbQueue> queueCount = DbQueue.getConsolidatedQueueCount(0);
		System.out.println("Total records: " + queueCount.size());
		for(DbQueue queue : queueCount)
		{
			System.out.printf("   * %s : %s %n", queue.getName(), queue.getCount());
		}
		System.out.println();
	}
	
	private static void showSubscriptiontList()
	{
		System.out.println("Subscriptiont List:");
		
		Collection<DbSubscription> allSubscriptionCount = DbSubscription.getAllSubscriptionCount();
		System.out.println("Total records: " + allSubscriptionCount.size());
		for(DbSubscription subscription : allSubscriptionCount)
		{
			System.out.printf("   * %s : %s : %s : %s  : %s %n", subscription.getAgentName(), subscription.getSubscription(), subscription.getSubscriptionType(), subscription.getCount(), subscription.getDate());
		}
		System.out.println();
	}
	
	private static void showConsolidatedSubscriptionList()
	{
		System.out.println("Consolidated Subscription List:");
		Collection<DbSubscription> subscriptionCount = DbSubscription.getConsolidatedSubscriptionCount();
		System.out.println("Total records: " + subscriptionCount.size());
		for(DbSubscription subscription : subscriptionCount)
		{
			System.out.printf("   * %s : %s : %s %n", subscription.getSubscription(), subscription.getSubscriptionType() , subscription.getCount());
		}
		System.out.println();
	}
	
	private static void showDropboxList()
	{
		System.out.println("Dropbox List:");
		
		Collection<DbDropbox> dropboxes = DbDropbox.getDropboxes();
		System.out.println("Total records: " + dropboxes.size());
		for(DbDropbox dropbox : dropboxes)
		{
			System.out.printf("   * %s : %s : %s : %s %n", dropbox.getAgentName(), dropbox.getDropboxLocation(), dropbox.getMessagesCount(), dropbox.getGoodMessagesCount());
		}
		System.out.println();		
	}
	
	private static void showFaultsList()
	{
		System.out.println("Faults List:");
		
		Collection<DbFault> allFaults = DbFault.getAllFaults();
		System.out.println("Total records: " + allFaults.size());
		for(DbFault fault : allFaults )
		{
			System.out.printf("   * %s %s %s %n", fault.getAgentName(), fault.getDate(), fault.getMessage().substring(0, 512));
		}
		System.out.println();
	}
}
