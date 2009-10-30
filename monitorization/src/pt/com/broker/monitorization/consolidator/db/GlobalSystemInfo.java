package pt.com.broker.monitorization.consolidator.db;

import java.util.ArrayList;
import java.util.Collection;

import pt.com.broker.monitorization.collectors.AgentStatus;

public class GlobalSystemInfo
{

	private static final int BIGGEST_QUEUES_COUNT = 20;
	private static final int LATEST_FAULTS_COUNT = 20;
	private static final int BIGGEST_DROPBOX_COUNT = 20;
	
	/*
	 *  Queues
	 */
	public static Collection<DbQueue> getBiggestQueues()
	{
		Collection<DbQueue> queueCollection = DbQueue.getConsolidatedQueueCount(0);
		Collection<DbQueue> biggest = new ArrayList<DbQueue>(BIGGEST_QUEUES_COUNT);
		for(DbQueue queue : queueCollection)
		{
			biggest.add(queue);
			if(biggest.size() == BIGGEST_QUEUES_COUNT)
				break;
		}
		
		return biggest;
	}
	public static Collection<DbQueue> getQueue(String queueName)
	{
		Collection<DbQueue> queueCollection = DbQueue.getQueue(queueName);		
		return queueCollection;
	}
	
	/*
	 *  Subscriptions
	 */
	public static Collection<DbSubscription> getSubscription(String subscriptionName)
	{
		Collection<DbSubscription> subscriptions = DbSubscription.getSubscription(subscriptionName);		
		return subscriptions;
	}
	
	public static Collection<DbSubscription> getSubscriptions()
	{
		Collection<DbSubscription> subscriptions = DbSubscription.getConsolidatedSubscriptionCount();
		return subscriptions;
	}
	
	/*
	 *  Faults
	 */
	public static Collection<DbFault> getLatestFaults()
	{
		Collection<DbFault> faultsCollection = DbFault.getAllFaults();
		Collection<DbFault> latest = new ArrayList<DbFault>(LATEST_FAULTS_COUNT);
		for(DbFault fault : faultsCollection)
		{
			latest.add(fault);
			if(latest.size() == LATEST_FAULTS_COUNT)
				break;
		}
		
		return latest;
	}
	
	public static Collection<DbFault> getFaultsFromAgent(String agentName)
	{
		Collection<DbFault> faultsCollection = DbFault.getAgentFaults(agentName);
		
		return faultsCollection;
	}
	
	public static DbFault getFault(int faultId)
	{
		DbFault fault = DbFault.getFault(faultId);
		if(fault == null)
			throw new RuntimeException("No fault wiht id " + faultId);
		return fault;
	}
	
	/*
	 *  Dropbox
	 */
	public static Collection<DbDropbox> getBiggestDropbox()
	{
		Collection<DbDropbox> dropboxCollection = DbDropbox.getDropboxesOrderByMessages();
		Collection<DbDropbox> dropboxes = new ArrayList<DbDropbox>(BIGGEST_DROPBOX_COUNT);
		for(DbDropbox dropbox : dropboxCollection)
		{
			dropboxes.add(dropbox);
			if(dropboxes.size() == BIGGEST_DROPBOX_COUNT)
				break;
		}
		
		return dropboxes;
	}
	
	public static Collection<DbDropbox> getBiggestDropboxOrderByTotalMessages()
	{
		Collection<DbDropbox> dropboxCollection = DbDropbox.getDropboxesOrderByMessages();
		Collection<DbDropbox> dropboxes = new ArrayList<DbDropbox>(BIGGEST_DROPBOX_COUNT);
		for(DbDropbox dropbox : dropboxCollection)
		{
			dropboxes.add(dropbox);
			if(dropboxes.size() == BIGGEST_DROPBOX_COUNT)
				break;
		}
		
		return dropboxes;
	}
	/*
	 *  Agents
	 */
	public static Collection<DbAgents> getAllAgents()
	{
		return DbAgents.getAllAgents();
	}
	public static Collection<DbAgents> getAgentsByStatus(AgentStatus status)
	{
		return DbAgents.getAgentsWithStatus(status);
	}
}
