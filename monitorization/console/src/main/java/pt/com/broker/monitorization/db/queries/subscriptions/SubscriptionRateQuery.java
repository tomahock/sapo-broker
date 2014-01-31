package pt.com.broker.monitorization.db.queries.subscriptions;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import org.caudexorigo.jdbc.Db;

import pt.com.broker.monitorization.db.queries.StaticQuery;
import pt.com.broker.monitorization.http.QueryStringParameters;

public abstract class SubscriptionRateQuery extends StaticQuery
{

	private final String QUERY_ALL;
	private final String QUERY_LAST;

	public SubscriptionRateQuery(String queryAll, String queryLast)
	{
		super();
		QUERY_ALL = queryAll;
		QUERY_LAST = queryLast;
	}

	public ResultSet getResultSet(Db db, Map<String, List<String>> params)
	{
		String windowParam = QueryStringParameters.getWindowParam(params);

		String subsName = QueryStringParameters.getSubscriptionNameParam(params);
		if (subsName == null)
		{
			return null;
		}

		if (windowParam != null)
		{
			if (windowParam.equals(QueryStringParameters.WINDOW_PARAM_ALL))
			{
				return db.runRetrievalPreparedStatement(QUERY_ALL, subsName);
			}
			else if (windowParam.equals(QueryStringParameters.WINDOW_PARAM_LAST))
			{
				return db.runRetrievalPreparedStatement(QUERY_LAST, subsName);
			}
			return null;
		}
		return db.runRetrievalPreparedStatement(QUERY_ALL, subsName);
	}
}