package pt.com.broker.monitorization.db.queries.queues;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import org.caudexorigo.jdbc.Db;

import pt.com.broker.monitorization.db.queries.StaticQuery;
import pt.com.broker.monitorization.http.QueryStringParameters;

public abstract class QueueRateQuery extends StaticQuery
{

	private final String QUERY_ALL;
	private final String QUERY_LAST;

	public QueueRateQuery(String queryAll, String queryLast)
	{
		super();
		QUERY_ALL = queryAll;
		QUERY_LAST = queryLast;
	}

	public ResultSet getResultSet(Db db, Map<String, List<String>> params)
	{
		String windowParam = QueryStringParameters.getWindowParam(params);

		String queueName = QueryStringParameters.getQueueNameParam(params);
		if (queueName == null)
		{
			return null;
		}

		if (windowParam != null)
		{
			if (windowParam.equals(QueryStringParameters.WINDOW_PARAM_ALL))
			{
				return db.runRetrievalPreparedStatement(QUERY_ALL, queueName);
			}
			else if (windowParam.equals(QueryStringParameters.WINDOW_PARAM_LAST))
			{
				return db.runRetrievalPreparedStatement(QUERY_LAST, queueName);
			}
			return null;
		}
		return db.runRetrievalPreparedStatement(QUERY_ALL, queueName);
	}
}