package pt.com.broker.monitorization.db.queries;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import org.caudexorigo.jdbc.Db;

import pt.com.broker.monitorization.http.QueryStringParameters;

public class OutputRateStaticQuery extends StaticQuery
{

	// private static final Logger log = LoggerFactory.getLogger(OutputRateStaticQuery.class);

	private static String QUERY_ALL = "SELECT last_event_ouput_message(generate_series, '00:06') FROM generate_series(now()- time '00:20',  now(), '00:01'::time)";
	private static String QUERY_LAST = "SELECT last_event_ouput_message(now(), '00:06')";

	@Override
	public String getId()
	{
		return "outputrate";
	}

	@Override
	public ResultSet getResultSet(Db db, Map<String, List<String>> params)
	{
		String windowParam = QueryStringParameters.getWindowParam(params);
		if (windowParam != null)
		{
			if (windowParam.equals(QueryStringParameters.WINDOW_PARAM_ALL))
			{
				return db.runRetrievalPreparedStatement(QUERY_ALL);
			}
			else if (windowParam.equals(QueryStringParameters.WINDOW_PARAM_LAST))
			{
				return db.runRetrievalPreparedStatement(QUERY_LAST);
			}
			return null;
		}
		return db.runRetrievalPreparedStatement(QUERY_ALL);
	}
}
