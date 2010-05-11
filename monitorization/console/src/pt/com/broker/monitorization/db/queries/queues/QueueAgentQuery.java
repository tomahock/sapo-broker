package pt.com.broker.monitorization.db.queries.queues;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import org.caudexorigo.jdbc.Db;
import org.caudexorigo.jdbc.DbPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueueAgentQuery
{
	private static final Logger log = LoggerFactory.getLogger(QueueAgentQuery.class);
	
	private static String AGENTNAME_PARAM = "agentname";
	private static String QUERY = "SELECT * FROM ( SELECT queues.subject , last_event_for_subject_predicate_agent(queues.subject, 'queue-size', ?, now(), '00:05') AS queuesize FROM (SELECT DISTINCT subject FROM raw_data WHERE agent_name = ? AND subject ~ '^queue://' AND event_time > now() - '00:05'::time) AS queues ) AS q WHERE queuesize IS NOT NULL ORDER BY queuesize DESC";

	public String getId()
	{
		return "queueAgentInfo";
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
				sb.append("\"queueName\":\"");
				String agentName = queryResult.getString(idx++);
				sb.append(agentName);
				sb.append("\",");

				sb.append("\"queueSize\":\"");
				sb.append(queryResult.getDouble(idx++));
				sb.append("\"");
				
				sb.append("}");
			}
		}
		catch (Throwable t)
		{
			log.error("Failed to get all queue genral info", t);
		}
		finally
		{
			DbPool.release(db);
		}

		return sb.toString();
	}

	protected ResultSet getResultSet(Db db, Map<String, List<String>> params)
	{
		List<String> list = params.get(AGENTNAME_PARAM);
		String agentName = null;
		if( (list != null) && list.size() == 1)
		{
			agentName = list.get(0);
		}
		if(agentName == null)
		{
			return null;
		}
		return db.runRetrievalPreparedStatement(QUERY, agentName, agentName);
	}
}
