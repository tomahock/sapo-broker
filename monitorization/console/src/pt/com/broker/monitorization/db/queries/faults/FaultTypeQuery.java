package pt.com.broker.monitorization.db.queries.faults;

import java.sql.ResultSet;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.caudexorigo.jdbc.Db;
import org.caudexorigo.jdbc.DbPool;
import org.caudexorigo.text.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.monitorization.AgentHostname;

public class FaultTypeQuery
{
	private static final Logger log = LoggerFactory.getLogger(FaultTypeQuery.class);

	private static String FAULTTYPE_PARAM = "type";
	private static String QUERY = "SELECT id, agent_name, event_time FROM fault_data  WHERE event_time > (now() -  '00:15'::time) AND short_message = ? ORDER BY event_time DESC LIMIT 10";

	public String getId()
	{
		return "faultTypeInfo";
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

				sb.append("\"id\":\"");
				sb.append(queryResult.getInt(idx++));
				sb.append("\",");

				sb.append("\"agentName\":\"");
				String agentName = queryResult.getString(idx++);
				sb.append(agentName);
				sb.append("\",");

				sb.append("\"agentHostname\":\"");
				sb.append(AgentHostname.get(agentName));
				sb.append("\",");

				sb.append("\"time\":\"");
				sb.append(DateUtil.formatISODate(new Date(queryResult.getTimestamp(idx++).getTime())));
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
		List<String> list = params.get(FAULTTYPE_PARAM);
		String faultType = null;
		if ((list != null) && list.size() == 1)
		{
			faultType = list.get(0);
		}
		if (faultType == null)
		{
			return null;
		}
		return db.runRetrievalPreparedStatement(QUERY, faultType);
	}
}
