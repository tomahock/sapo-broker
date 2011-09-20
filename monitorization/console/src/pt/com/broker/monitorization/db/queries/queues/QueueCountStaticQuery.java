package pt.com.broker.monitorization.db.queries.queues;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import org.caudexorigo.jdbc.Db;

import pt.com.broker.monitorization.db.queries.StaticQuery;
import pt.com.broker.monitorization.http.QueryStringParameters;

public class QueueCountStaticQuery extends StaticQuery
{
	// private static final Logger log = LoggerFactory.getLogger(QueueCountStaticQuery.class);

	private static String QUERY_ALL = "SELECT last_event_for_predicate(?, generate_series, '00:00:20') FROM generate_series(now()- '00:10'::time,  now(), '00:00:20'::time)";
	private static String QUERY_LAST = "SELECT last_event_for_predicate(?, now(), '00:01')";

	private static final String predicate = "queue-size";

	@Override
	public String getId()
	{
		return "queuecount";
	}

	@Override
	public ResultSet getResultSet(Db db, Map<String, List<String>> params)
	{
		String windowParam = QueryStringParameters.getWindowParam(params);
		if (windowParam != null)
		{
			if (windowParam.equals(QueryStringParameters.WINDOW_PARAM_ALL))
			{
				return db.runRetrievalPreparedStatement(QUERY_ALL, predicate);
			}
			else if (windowParam.equals(QueryStringParameters.WINDOW_PARAM_LAST))
			{
				return db.runRetrievalPreparedStatement(QUERY_LAST, predicate);
			}
			return null;
		}
		return db.runRetrievalPreparedStatement(QUERY_ALL, predicate);
	}

}
