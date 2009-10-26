package pt.com.broker.monitorization.http;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.caudexorigo.text.StringUtils;

import pt.com.broker.monitorization.collectors.JsonEncodable;
import pt.com.broker.monitorization.consolidator.db.DbDropbox;
import pt.com.broker.monitorization.consolidator.db.DbFault;
import pt.com.broker.monitorization.consolidator.db.DbQueue;
import pt.com.broker.monitorization.consolidator.db.DbSubscription;
import pt.com.broker.monitorization.consolidator.db.GlobalSystemInfo;

public class DataFetcher
{
	public interface ItemFetcher
	{
		Collection<JsonEncodable> getData(String resource, Map<String, List<String>> arguments);
	}

	private static Map<String, ItemFetcher> fetchers = new HashMap<String, ItemFetcher>();
	
	static
	{
		fetchers.put("faults", new ItemFetcher(){
			@Override
			public Collection<JsonEncodable> getData(String resource,Map<String, List<String>> arguments)
			{
				Collection<DbFault> latestDbFaults = GlobalSystemInfo.getLatestFaults();

				Collection<JsonEncodable> faults = new ArrayList<JsonEncodable>(latestDbFaults.size());
				for(DbFault fault : latestDbFaults)
				{
					faults.add(fault);
				}
				return faults;
			}
		});
		fetchers.put("queues", new ItemFetcher(){
			final String AGENTS_PREFIX = "agents";
			final String SUBSCRIPTIONS_PREFIX = "subscriptions";
			
			final String QUEUE_NAME = "queuename";
			
			@Override
			public Collection<JsonEncodable> getData(String resource, Map<String, List<String>> arguments)
			{
				Collection<JsonEncodable> queuesInfo = new ArrayList<JsonEncodable>();
				
				if( resource.startsWith(AGENTS_PREFIX) )
				{
					List<String> queueNameList = arguments.get(QUEUE_NAME);
					if (queueNameList == null)
						return queuesInfo;
					
					String queueName = queueNameList.get(0);
					Collection<DbQueue> queueCollection = GlobalSystemInfo.getQueue(queueName);
					for(DbQueue queue : queueCollection)
					{
						queuesInfo.add(queue);
					}					
				}
				else if (resource.startsWith(SUBSCRIPTIONS_PREFIX) )
				{
					List<String> queueNameList = arguments.get(QUEUE_NAME);
					if (queueNameList == null)
						return queuesInfo;
					
					String queueName = queueNameList.get(0);
					Collection<DbSubscription> subscriptionCollection = GlobalSystemInfo.getSubscription(queueName);
					for(DbSubscription subscription : subscriptionCollection)
					{
						queuesInfo.add(subscription);
					}
				}
				else 
				{
					Collection<DbQueue> biggestQueues = GlobalSystemInfo.getBiggestQueues();

					for(DbQueue queue : biggestQueues)
					{
						queuesInfo.add(queue);
					}	
				}
				
				return queuesInfo;
			}
		});
		fetchers.put("dropbox", new ItemFetcher(){
			@Override
			public Collection<JsonEncodable> getData(String resource, Map<String, List<String>> arguments)
			{
				Collection<DbDropbox> biggestDropboxes = GlobalSystemInfo.getBiggestDropbox();

				Collection<JsonEncodable> dropboxes = new ArrayList<JsonEncodable>(biggestDropboxes.size());
				for(DbDropbox dropbox : biggestDropboxes)
				{
					dropboxes.add(dropbox);
				}
				return dropboxes;
			}
		});
		fetchers.put("subscriptions", new ItemFetcher(){
			@Override
			public Collection<JsonEncodable> getData(String resource, Map<String, List<String>> arguments)
			{
				Collection<DbSubscription> allSubscriptions = GlobalSystemInfo.getSubscriptions();

				Collection<JsonEncodable> subscriptions = new ArrayList<JsonEncodable>(allSubscriptions.size());
				for(JsonEncodable subscription : allSubscriptions)
				{
					subscriptions.add(subscription);
				}
				return subscriptions;
			}
		});
	}
	
	public static String getData(String resource, Map<String, List<String>> arguments)
	{
		String[] parts = resource.split("/");
		ItemFetcher itemFetcher = fetchers.get(parts[0]);
		if(itemFetcher == null)
		{
			return String.format("{\"Unknown data identifier - %s\"}", resource);
		}
		
		int offset = parts.length > 1 ? 1 : 0;
		
		Collection<JsonEncodable> dataItems = itemFetcher.getData(resource.substring(parts[0].length() + offset), arguments);
		return JsonUtil.getJsonEncodedCollection(dataItems);		
	}
}
