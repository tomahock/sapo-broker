package pt.com.broker.monitorization.db.queries.faults;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import org.caudexorigo.jdbc.Db;
import org.caudexorigo.jdbc.DbPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AllFaultTypeQuery
{
	private static final Logger log = LoggerFactory.getLogger(AllFaultTypeQuery.class);

	private static String QUERY = "SELECT short_message, count (id) AS count FROM fault_data WHERE event_time > (now() - '00:15'::time) GROUP BY short_message ORDER BY count DESC";

	public String getId()
	{
		return "allFaultTypeInfo";
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

				sb.append("\"shortMessage\":\"");
				sb.append(queryResult.getString(idx++));
				sb.append("\",");

				sb.append("\"count\":\"");
				sb.append(queryResult.getInt(idx++));
				sb.append("\"");

				sb.append("}");
			}
		}
		catch (Throwable t)
		{
			log.error("Failed to get agent's fault info.", t);
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
