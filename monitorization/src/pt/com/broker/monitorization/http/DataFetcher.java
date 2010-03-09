package pt.com.broker.monitorization.http;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.monitorization.collectors.AgentStatus;
import pt.com.broker.monitorization.collectors.JsonEncodable;
import pt.com.broker.monitorization.consolidator.db.DbAgents;
import pt.com.broker.monitorization.consolidator.db.DbDropbox;
import pt.com.broker.monitorization.consolidator.db.DbFault;
import pt.com.broker.monitorization.consolidator.db.DbQueue;
import pt.com.broker.monitorization.consolidator.db.DbSubscription;
import pt.com.broker.monitorization.consolidator.db.GlobalSystemInfo;

public class DataFetcher
{
	private static final Logger log = LoggerFactory.getLogger(DataFetcher.class);

	public interface ItemFetcher
	{
		Collection<JsonEncodable> getData(String resource, Map<String,String> arguments);
	}

	private static Map<String, ItemFetcher> fetchers = new HashMap<String, ItemFetcher>();

	static
	{
		fetchers.put("faults", new ItemFetcher()
		{
			final String AGENTS_PREFIX = "agent";
			final String FAULTID_PREFIX = "faultid";

			final String FAULT_ID = "id";
			final String AGENT_NAME = "agentname";

			@Override
			public Collection<JsonEncodable> getData(String resource, Map<String, String> arguments)
			{
				Collection<JsonEncodable> faults = new ArrayList<JsonEncodable>();
				try
				{
					if (resource.startsWith(FAULTID_PREFIX))
					{
						String idArg = arguments.get(FAULT_ID);
						if (idArg == null)
						{
							return faults;
						}
						int id = Integer.parseInt(idArg);

						DbFault fault = GlobalSystemInfo.getFault(id);
						faults.add(fault);
					}
					else if (resource.startsWith(AGENTS_PREFIX))
					{
						String agentArg = arguments.get(AGENT_NAME);
						if (agentArg == null)
						{
							return faults;
						}
						String agentName = agentArg;

						Collection<DbFault> dbFaults = GlobalSystemInfo.getFaultsFromAgent(agentName);
						for (DbFault fault : dbFaults)
							faults.add(fault);
					}
					else
					{
						Collection<DbFault> latestDbFaults = GlobalSystemInfo.getLatestFaults();

						for (DbFault fault : latestDbFaults)
						{
							faults.add(fault);
						}
					}
				}
				catch (Throwable t)
				{
					log.error("Error while fetching fault", t);
				}
				return faults;
			}
		});
		fetchers.put("queues", new ItemFetcher()
		{
			final String AGENTS_PREFIX = "agents";
			final String SUBSCRIPTIONS_PREFIX = "subscriptions";
			final String ALL_PREFIX = "all";
			

			final String QUEUE_NAME = "queuename";

			@Override
			public Collection<JsonEncodable> getData(String resource, Map<String, String> arguments)
			{
				Collection<JsonEncodable> queuesInfo = new ArrayList<JsonEncodable>();

				if (resource.startsWith(AGENTS_PREFIX))
				{
					String queueName = arguments.get(QUEUE_NAME);
					if (queueName == null)
						return queuesInfo;

					Collection<DbQueue> queueCollection = GlobalSystemInfo.getQueue(queueName);
					for (DbQueue queue : queueCollection)
					{
						queuesInfo.add(queue);
					}
				}
				else if (resource.startsWith(SUBSCRIPTIONS_PREFIX))
				{
					String queueName = arguments.get(QUEUE_NAME);
					if (queueName == null)
						return queuesInfo;

					Collection<DbSubscription> subscriptionCollection = GlobalSystemInfo.getSubscription(queueName);
					for (DbSubscription subscription : subscriptionCollection)
					{
						queuesInfo.add(subscription);
					}
				}
				else if (resource.startsWith(ALL_PREFIX))
				{
					Collection<DbQueue> queueCollection = GlobalSystemInfo.getAllQueues();
					for (DbQueue dbQueue : queueCollection)
					{
						queuesInfo.add(dbQueue);
					}
				}
				else
				{
					Collection<DbQueue> biggestQueues = GlobalSystemInfo.getBiggestQueues();

					for (DbQueue queue : biggestQueues)
					{
						queuesInfo.add(queue);
					}
				}

				return queuesInfo;
			}
		});
		fetchers.put("dropbox", new ItemFetcher()
		{
			final String AGENTS_PREFIX = "agent";
			
			final String AGENT_NAME = "agentname";
			@Override
			public Collection<JsonEncodable> getData(String resource, Map<String, String> arguments)
			{
				Collection<JsonEncodable> dropboxes = new ArrayList<JsonEncodable>();
				Collection<DbDropbox> biggestDropboxes = null;
				if(resource.startsWith(AGENTS_PREFIX))
				{
					String agentName = arguments.get(AGENT_NAME);
					if (agentName == null)
						return dropboxes;
					
					biggestDropboxes = GlobalSystemInfo.getAgentDropbox(agentName);
				}
				else
				{
					biggestDropboxes = GlobalSystemInfo.getBiggestDropbox();
				}

				
				for (DbDropbox dropbox : biggestDropboxes)
				{
					dropboxes.add(dropbox);
				}
				return dropboxes;
			}
		});
		fetchers.put("subscriptions", new ItemFetcher()
		{
			final String AGENTS_PREFIX = "agent";
			
			final String AGENT_NAME = "agentname";

			@Override
			public Collection<JsonEncodable> getData(String resource, Map<String, String> arguments)
			{
				Collection<JsonEncodable> subscriptions = new ArrayList<JsonEncodable>();
				Collection<DbSubscription> allSubscriptions = null;
				if (resource.startsWith(AGENTS_PREFIX))
				{
					String agentName = arguments.get(AGENT_NAME);
					if (agentName == null)
						return subscriptions;
					
					allSubscriptions = GlobalSystemInfo.getAgentSubscriptions(agentName);
				}else if(resource.startsWith(AGENTS_PREFIX))
				{
					
				}
				else
				{
					allSubscriptions = GlobalSystemInfo.getSubscriptions();
				}
				for (JsonEncodable subscription : allSubscriptions)
				{
					subscriptions.add(subscription);
				}
				return subscriptions;
			}
		});

		fetchers.put("agents", new ItemFetcher()
		{
			final String OK_STATUS_PREFIX = "ok";
			final String DOWN_STATUS_PREFIX = "down";
			final String AGENT_PREFIX = "agent";
			final String HOST_PREFIX = "host";
			
			final String AGENT_NAME = "agentname";

			@Override
			public Collection<JsonEncodable> getData(String resource, Map<String, String> arguments)
			{
				Collection<JsonEncodable> jsonAgents = new ArrayList<JsonEncodable>();
				Collection<DbAgents> agents = null;
				if (resource.startsWith(OK_STATUS_PREFIX))
				{
					agents = GlobalSystemInfo.getAgentsByStatus(AgentStatus.Ok);
				}
				else if (resource.startsWith(DOWN_STATUS_PREFIX))
				{
					agents = GlobalSystemInfo.getAgentsByStatus(AgentStatus.Down);
				}
				else if(resource.startsWith(AGENT_PREFIX))
				{
					String agentName = arguments.get(AGENT_NAME);
					if (agentName== null)
						return jsonAgents;
					
					agents = GlobalSystemInfo.getAgentsStatus(agentName);
				}
				else if(resource.startsWith(HOST_PREFIX))
				{
					String agentName = arguments.get(AGENT_NAME);
					if (agentName == null)
						return jsonAgents;
					
					agents = GlobalSystemInfo.getHostname(agentName);
				}
				
				else
				{
					agents = GlobalSystemInfo.getAllAgents();
				}
				for (JsonEncodable agent : agents)
				{
					jsonAgents.add(agent);
				}
				return jsonAgents;
			}
		});

	}

	public static String getData(String resource, Map<String, String> arguments)
	{
		String[] parts = resource.split("/");
		ItemFetcher itemFetcher = fetchers.get(parts[0]);
		if (itemFetcher == null)
		{
			return String.format("{\"Unknown data identifier - %s\"}", resource);
		}

		int offset = parts.length > 1 ? 1 : 0;

		Collection<JsonEncodable> dataItems = itemFetcher.getData(resource.substring(parts[0].length() + offset), arguments);
		return JsonUtil.getJsonEncodedCollection(dataItems);
	}
}
