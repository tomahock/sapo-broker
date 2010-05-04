package pt.com.broker.monitorization.db.queries;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import org.caudexorigo.jdbc.Db;
import org.caudexorigo.jdbc.DbPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AllQueuesGeneralInfoQuery
{
	private static final Logger log = LoggerFactory.getLogger(GeneralQueueInfoQuery.class);
	
	private static String QUERY = "SELECT  queue.subject AS queuename , last_event_for_subject_and_predicate(queue.subject, 'queue-size', now(), '00:10') AS queuesize , last_event_for_subject_and_predicate(queue.subject, 'input-rate', now(), '00:10') AS inputrate  , last_event_for_subject_and_predicate(queue.subject, 'output-rate', now(), '00:10') AS outputrate , last_event_for_subject_and_predicate(queue.subject, 'failed-rate', now(), '00:10') AS failed , last_event_for_subject_and_predicate(queue.subject, 'expired-rate', now(), '00:10') AS expired , last_event_for_subject_and_predicate(queue.subject, 'redelivered-rate', now(), '00:10') AS redelivered , last_event_for_subject_and_predicate(queue.subject, 'subscriptions', now(), '00:10') AS subscriptions FROM (SELECT DISTINCT subject FROM raw_data WHERE event_time > (now() - time '00:10') AND subject~'queue://') AS queue ORDER BY 2 DESC";

	public String getId()
	{
		return "allQueueGeneralInfo";
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
				String queuename = queryResult.getString(idx++);
				sb.append(queuename);
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
		return db.runRetrievalPreparedStatement(QUERY);
	}
}
