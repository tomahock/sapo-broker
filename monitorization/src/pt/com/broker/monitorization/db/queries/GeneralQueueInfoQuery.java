package pt.com.broker.monitorization.db.queries;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import org.caudexorigo.jdbc.Db;
import org.caudexorigo.jdbc.DbPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.monitorization.AgentHostname;

public class GeneralQueueInfoQuery
{
	private static final Logger log = LoggerFactory.getLogger(GeneralQueueInfoQuery.class);
	
	private static final String QUEUENAME_PARAM = "queuename";
	
	private static String QUERY = "SELECT \n	agents.agent_name\n	, last_event_for_subject_predicate_agent(?, 'queue-size', agents.agent_name, now()) AS queuesize\n	, last_event_for_subject_predicate_agent(?, 'input-rate', agents.agent_name, now()) AS inputrate \n	, last_event_for_subject_predicate_agent(?, 'output-rate', agents.agent_name, now()) AS outputrate\n	, last_event_for_subject_predicate_agent(?, 'failed-rate', agents.agent_name, now()) AS failed\n	, last_event_for_subject_predicate_agent(?, 'expired-rate', agents.agent_name, now()) AS expired\n	, last_event_for_subject_predicate_agent(?, 'redelivered-rate', agents.agent_name, now()) AS redelivered\n	, last_event_for_subject_predicate_agent(?, 'subscriptions', agents.agent_name, now()) AS subscriptions\nFROM (SELECT DISTINCT agent_name FROM raw_data WHERE event_time > (now() - time '00:35') AND subject=?) AS agents\nOrder BY 2 DESC";

	public String getId()
	{
		return "generalQueueInfo";
	}

	public String getJsonData(Map<String, List<String>> params)
	{
		Db db = null;

		StringBuilder sb = new StringBuilder();

		try
		{
			db = DbPool.obtain();

			ResultSet queryResult = getResultSet(db, params);
			if (queryResult == null)
				return "";

			boolean first = true;

			while (queryResult.next())
			{
				if (first)
				{
					first = false;
				}
				else
				{
					sb.append(",");
				}
				int idx = 1;
				sb.append("{");
				sb.append("\"agentName\":\"");
				String agentname = queryResult.getString(idx++);
				sb.append(agentname);
				sb.append("\",");

				sb.append("\"agentHostname\":\"");
				sb.append(AgentHostname.get(agentname));
				sb.append("\",");

				sb.append("\"queueSize\":\"");
				sb.append(queryResult.getDouble(idx++));
				sb.append("\",");

				sb.append("\"inputRate\":\"");
				sb.append(queryResult.getDouble(idx++));
				sb.append("\",");
				
				sb.append("\"outputRate\":\"");
				sb.append(queryResult.getDouble(idx++));
				sb.append("\",");
				
				sb.append("\"failedRate\":\"");
				sb.append(queryResult.getDouble(idx++));
				sb.append("\",");
				
				sb.append("\"expiredRate\":\"");
				sb.append(queryResult.getDouble(idx++));
				sb.append("\",");
				
				sb.append("\"redeliveredRate\":\"");
				sb.append(queryResult.getDouble(idx++));
				sb.append("\",");
				
				sb.append("\"subscriptions\":\"");
				sb.append(queryResult.getDouble(idx++));
				sb.append("\"");
				
				sb.append("}");
			}
		}
		catch (Throwable t)
		{
			log.error("Failed to get queue general info.", t);
		}
		finally
		{
			DbPool.release(db);
		}

		return sb.toString();
	}

	protected ResultSet getResultSet(Db db, Map<String, List<String>> params)
	{
		
		List<String> list = params.get(QUEUENAME_PARAM);
		if( list== null)
		{
			return null;
		}
		if(list.size() != 1)
		{
			return null;
		}
		String queuename = list.get(0);
		if(!isValid(queuename))
		{
			return null;
		}
		
		queuename = "queue://" + queuename;
		
		return db.runRetrievalPreparedStatement(QUERY, queuename, queuename, queuename, queuename, queuename, queuename, queuename, queuename);
	}

	private boolean isValid(String queuename)
	{
		//TODO: check queuename
		return true;
	}

}
