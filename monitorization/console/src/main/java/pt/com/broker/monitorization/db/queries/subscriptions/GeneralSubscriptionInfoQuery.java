package pt.com.broker.monitorization.db.queries.subscriptions;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import org.caudexorigo.jdbc.Db;
import org.caudexorigo.jdbc.DbPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.monitorization.AgentHostname;

public class GeneralSubscriptionInfoQuery
{
	private static final Logger log = LoggerFactory.getLogger(GeneralSubscriptionInfoQuery.class);

	private final static String SUBSCRIPTION_PARAM = "subscriptionname";

	private static String QUERY = "SELECT \n	agents.agent_name,\n	last_event_for_subject_predicate_agent(?, 'output-rate', agents.agent_name, now(), '00:05') AS outputrate,\n	last_event_for_subject_predicate_agent(?, 'discarded-rate', agents.agent_name, now(), '00:05') AS failed,\n	last_event_for_subject_predicate_agent(?, 'dispatched-to-queue-rate', agents.agent_name, now(), '00:05') AS expired,\n	last_event_for_subject_predicate_agent(?, 'subscriptions', agents.agent_name, now(), '00:05') AS subscriptions\nFROM (SELECT DISTINCT agent_name FROM raw_data WHERE event_time > (now() - '00:05'::time) AND subject=? AND object_value > 0) AS agents\nORDER BY 2 DESC";

	public String getId()
	{
		return "generalSubscriptionInfo";
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
				int idx = 1;
				sb.append("{");
				sb.append("\"agentName\":\"");
				String agentname = queryResult.getString(idx++);
				sb.append(agentname);
				sb.append("\",");

				sb.append("\"agentHostname\":\"");
				sb.append(AgentHostname.get(agentname));
				sb.append("\",");

				sb.append("\"outputRate\":\"");
				sb.append(queryResult.getDouble(idx++));
				sb.append("\",");

				sb.append("\"discardedRate\":\"");
				sb.append(queryResult.getDouble(idx++));
				sb.append("\",");

				sb.append("\"dispatchedToQueueRate\":\"");
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
			log.error("Failed to get subscription general info.", t);
		}
		finally
		{
			DbPool.release(db);
		}

		return sb.toString();
	}

	public static String getsubscription(Map<String, List<String>> params)
	{
		List<String> list = params.get(SUBSCRIPTION_PARAM);
		if ((list != null) && (list.size() == 1))
		{
			return "topic://" + list.get(0);
		}
		return null;
	}

	protected ResultSet getResultSet(Db db, Map<String, List<String>> params)
	{
		String subscriptionName = getsubscription(params);
		if (subscriptionName == null)
		{
			return null;
		}

		return db.runRetrievalPreparedStatement(QUERY, subscriptionName, subscriptionName, subscriptionName, subscriptionName, subscriptionName);
	}
}