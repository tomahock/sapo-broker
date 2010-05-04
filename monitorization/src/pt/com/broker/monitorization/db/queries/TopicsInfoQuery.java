package pt.com.broker.monitorization.db.queries;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import org.caudexorigo.jdbc.Db;
import org.caudexorigo.jdbc.DbPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TopicsInfoQuery
{
	private static final Logger log = LoggerFactory.getLogger(GeneralQueueInfoQuery.class);
	
	private static String QUERY = "SELECT \n	subscriptions.subject AS subscription\n	, last_event_for_subject_and_predicate(subscriptions.subject,'output-rate', now()) as outputrate\n	, last_event_for_subject_and_predicate(subscriptions.subject, 'subscriptions', now()) AS subscription_count\nFROM (SELECT DISTINCT subject FROM raw_data WHERE predicate = 'subscriptions' AND subject ~ '^topic://' AND event_time > (now() - time '00:10')) AS subscriptions\nORDER BY 3 DESC;";

	public String getId()
	{
		return "allSubscriptionsGeneralInfo";
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
				sb.append("\"subscriptionName\":\"");
				String queuename = queryResult.getString(idx++);
				sb.append(queuename);
				sb.append("\",");

				sb.append("\"outputRate\":\"");
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
