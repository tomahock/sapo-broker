/**
 * 
 */
package pt.com.broker.monitorization.db.queries;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import org.caudexorigo.jdbc.Db;
import org.caudexorigo.jdbc.DbPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class StaticQuery
{
	private static final Logger log = LoggerFactory.getLogger(StaticQuery.class);

	abstract public String getId();

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
				sb.append("{\"value\":\"");
				sb.append(queryResult.getDouble(1));
				sb.append("\"}");
			}
		}
		catch (Throwable t)
		{
			log.error("Failed to get queue count.", t);
		}
		finally
		{
			DbPool.release(db);
		}

		return sb.toString();
	}

	abstract protected ResultSet getResultSet(Db db, Map<String, List<String>> params);
}