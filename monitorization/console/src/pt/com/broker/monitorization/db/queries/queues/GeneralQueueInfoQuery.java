package pt.com.broker.monitorization.db.queries.queues;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import org.caudexorigo.jdbc.Db;
import org.caudexorigo.jdbc.DbPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.monitorization.AgentHostname;
import pt.com.broker.monitorization.http.QueryStringParameters;

public class GeneralQueueInfoQuery
{
	private static final Logger log = LoggerFactory.getLogger(GeneralQueueInfoQuery.class);

	private static String QUERY = "SELECT  agents.agent_name,\n last_event_for_subject_predicate_agent(?, 'queue-size', agents.agent_name, now(), '00:05') AS queuesize,\n last_event_for_subject_predicate_agent(?, 'input-rate', agents.agent_name, now(), '00:05') AS inputrate,\n last_event_for_subject_predicate_agent(?, 'output-rate', agents.agent_name, now(), '00:05') AS outputrate,\n last_event_for_subject_predicate_agent(?, 'expired-rate', agents.agent_name, now(), '00:05') AS expired,\n last_event_for_subject_predicate_agent(?, 'redelivered-rate', agents.agent_name, now(), '00:05') AS redelivered,\n last_event_for_subject_predicate_agent(?, 'subscriptions', agents.agent_name, now(), '00:05') AS subscriptions \nFROM (SELECT DISTINCT agent_name FROM raw_data WHERE event_time > (now() - '00:05'::time) AND object_value > 0 AND subject=?) AS agents Order BY 2 DESC";

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
			db = DbPool.pick();

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
				sb.append("{");
				sb.append("\"agentName\":\"");
				String agentname = queryResult.getString(1);
				sb.append(agentname);
				sb.append("\",");

				sb.append("\"agentHostname\":\"");
				sb.append(AgentHostname.get(agentname));
				sb.append("\",");

				sb.append("\"queueSize\":\"");
				sb.append(queryResult.getDouble(2));
				sb.append("\",");

				sb.append("\"inputRate\":\"");
				sb.append(queryResult.getDouble(3));
				sb.append("\",");

				sb.append("\"outputRate\":\"");
				sb.append(queryResult.getDouble(4));
				sb.append("\",");

				sb.append("\"expiredRate\":\"");
				sb.append(queryResult.getDouble(5));
				sb.append("\",");

				sb.append("\"redeliveredRate\":\"");
				sb.append(queryResult.getDouble(5));
				sb.append("\",");

				sb.append("\"subscriptions\":\"");
				sb.append(queryResult.getDouble(7));
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

		String queuename = QueryStringParameters.getQueueNameParam(params);
		if (queuename == null)
		{
			return null;
		}

		return db.runRetrievalPreparedStatement(QUERY, queuename, queuename, queuename, queuename, queuename, queuename, queuename);
	}
}
