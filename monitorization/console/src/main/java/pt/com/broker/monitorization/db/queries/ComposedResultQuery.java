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

public abstract class ComposedResultQuery
{
	private static final Logger log = LoggerFactory.getLogger(ComposedResultQuery.class);

	private String QUERY;

	protected ComposedResultQuery(String query)
	{
		this.QUERY = query;
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
				getElement(sb, queryResult);
			}
		}
		catch (Throwable t)
		{
			log.error(String.format("Failed to get result for query '%s'", QUERY), t);
		}
		finally
		{
			DbPool.release(db);
		}

		return sb.toString();
	}

	abstract public String getId();

	abstract protected void getElement(StringBuilder stringBuilder, ResultSet queryResult) throws Throwable;

	protected ResultSet getResultSet(Db db, Map<String, List<String>> params)
	{
		return db.runRetrievalPreparedStatement(QUERY);
	}
}