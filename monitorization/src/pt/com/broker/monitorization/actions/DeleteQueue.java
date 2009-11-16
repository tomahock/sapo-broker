package pt.com.broker.monitorization.actions;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.text.StyledEditorKit.BoldAction;

import org.caudexorigo.concurrent.CustomExecutors;
import org.caudexorigo.text.StringUtils;
import org.jboss.netty.util.internal.ReusableIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.client.HostInfo;
import pt.com.broker.monitorization.Utils;
import pt.com.broker.monitorization.collectors.JsonEncodable;
import pt.com.broker.monitorization.configuration.ConfigurationInfo;
import pt.com.broker.monitorization.configuration.ConfigurationInfo.AgentInfo;

public class DeleteQueue
{
	private static final Logger log = LoggerFactory.getLogger(DeleteQueue.class);

	private static final String FAIL_TO_CONNECT_MESSAGE = "Failed to connect to server.";
	
	//private static final String ACTIVE_CONSUMERS_ERROR_SUFIX  = "has active consumers.";
	private static final String QUEUE_DOESNOT_EXIST_ERROR_SUFIX = " doesn't exist.";
	
	public static class DeleteResult  implements JsonEncodable
	{
		private final String agentName;
		private final boolean success;
		private String reason;

		public DeleteResult(String agentName, boolean sucess)
		{
			this.agentName = agentName;
			this.success = sucess;
		}

		public String getAgentName()
		{
			return agentName;
		}

		public boolean isSuccess()
		{
			return success;
		}

		public String getReason()
		{
			return reason;
		}

		public void setReason(String reason)
		{
			this.reason = reason;
		}

		@Override
		public String toJson()
		{
			if (reason == null)
				return String.format("{\"agentName\":\"%s\",\"sucess\":\"%s\"}", this.agentName, this.success + "");
			else
				return String.format("{\"agentName\":\"%s\",\"sucess\":\"%s\",\"reason\":\"%s\"}", this.agentName, this.success + "", this.reason);
		}

	}

	
	private static class DeferedDelete 
	{
		final AgentInfo agentInfo;
		final String queueName;
		volatile int count;
		
		DeferedDelete(AgentInfo agent, String queueName)
		{
			this.agentInfo = agent;
			this.queueName = queueName;
			count = 0;
		}
	}
	
	private final static ScheduledThreadPoolExecutor shed_exec_srv;
	
	static
	{
		Runnable task = new Runnable()
		{
			private final int MAX_DELETE_TRIES = 5;
			
			@Override
			public void run()
			{
				log.debug("Running pending delete attempt");
				
				List<DeferedDelete> sucess = new ArrayList<DeferedDelete>();
				synchronized (deferedDeletes)
				{
					for(DeferedDelete dd : deferedDeletes)
					{
						HostInfo hostInfo = dd.agentInfo.httpInfo;

						String result = deleteQueue(hostInfo.getHostname(), hostInfo.getPort()+"", dd.queueName);
						DeleteResult delResult = interpretResult(hostInfo.getHostname(), result);
						if(delResult.isSuccess())
						{
							sucess.add(dd);
						}
						else
						{
							if(++dd.count == MAX_DELETE_TRIES)
							{
								log.debug("Giving up queue '{}' removal from agent '{}'.", dd.queueName, dd.queueName);
								sucess.add(dd); // not a success, but we're giving up
							}
						}
					}
					for(DeferedDelete dd : sucess)
					{
						deferedDeletes.remove(dd);
					}
				}
				
			}
		};
		
		shed_exec_srv = CustomExecutors.newScheduledThreadPool(10, "Defered Deletes");
		
		shed_exec_srv.scheduleAtFixedRate(task, 1, 1, TimeUnit.MINUTES);		
	}
	
	
	private static List<DeferedDelete>  deferedDeletes = new LinkedList<DeferedDelete>(); 
	
	public static List<DeleteResult> execute(String queueName)
	{
		List<AgentInfo> cloudAgents = ConfigurationInfo.getCloudAgents();

		List<DeleteResult> results = new ArrayList<DeleteResult>(cloudAgents.size());

		for (AgentInfo agent : cloudAgents)
		{
			HostInfo hostInfo = agent.httpInfo;

			String result = deleteQueue(hostInfo.getHostname(), hostInfo.getPort()+"", queueName);

			if(log.isDebugEnabled())
			{
				log.debug("Deleting queue '{}' from agent '{}'. Delete Result: " + result, queueName, agent.hostname);
			}
			
			DeleteResult delResult = interpretResult(agent.hostname, result);
			
			results.add(delResult);
			
			if( delResult.isSuccess() || ((!delResult.isSuccess()) && delResult.getReason().endsWith(QUEUE_DOESNOT_EXIST_ERROR_SUFIX)) )
			{
				// Success or queue dosen't exist.
				;
			}
			else
			{
				deferDelete(agent, queueName);
			}
		}
		return results;
	}

	private static void deferDelete(AgentInfo agent, String queueName)
	{
		synchronized (deferedDeletes)
		{
			deferedDeletes.add(new DeferedDelete(agent, queueName));
		}
	}

	private static String deleteQueue(String hostname, String port, String queueName)
	{
		String result = FAIL_TO_CONNECT_MESSAGE;
		try
		{
			String agentUrl = String.format("http://%s:%s/broker/admin", hostname, port);

			URL url = new URL(agentUrl);
			URLConnection connection = url.openConnection();

			HttpURLConnection httpUrlconn = (HttpURLConnection) connection;
			
			httpUrlconn.setDoOutput(true);
			httpUrlconn.setConnectTimeout(500);
			httpUrlconn.setReadTimeout(60000);

			OutputStreamWriter wr = new OutputStreamWriter(httpUrlconn.getOutputStream());
			wr.write("QUEUE:" + queueName);

			wr.flush();

			int respCode = httpUrlconn.getResponseCode();
			InputStream is = null;
			if(respCode == HttpURLConnection.HTTP_OK)
			{
				is = httpUrlconn.getInputStream();
			}
			else
			{
				is = httpUrlconn.getErrorStream();
			}
			
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			result = rd.readLine();

			wr.close();
			rd.close();
		}
		catch (Throwable t)
		{
			log.error(String.format("Failed to connect to agent '%s:%s' to delete queue '%s'.", hostname, port, queueName), t);
		}

		return result;
	}
	
	private static DeleteResult interpretResult(String hostname, String result)
	{
		final String ERROR_PREFIX= "Error: ";
		
		DeleteResult deleteResult = null;
		
		if(result.equals(FAIL_TO_CONNECT_MESSAGE))
		{
			deleteResult = new DeleteResult(hostname, false);
			deleteResult.setReason(FAIL_TO_CONNECT_MESSAGE);
		}
		else if(result.startsWith(ERROR_PREFIX))
		{
			deleteResult = new DeleteResult(hostname, false);
			deleteResult.setReason(StringUtils.substringAfter(result, ERROR_PREFIX));
		}
		else
		{
			deleteResult = new DeleteResult(hostname, true);
		}
				
		return deleteResult;
	}
	
}
