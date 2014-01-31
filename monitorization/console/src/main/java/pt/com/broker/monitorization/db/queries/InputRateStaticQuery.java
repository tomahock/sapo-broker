package pt.com.broker.monitorization.db.queries;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import org.caudexorigo.jdbc.Db;

import pt.com.broker.monitorization.http.QueryStringParameters;

public class InputRateStaticQuery extends StaticQuery
{
	// private static final Logger log = LoggerFactory.getLogger(InputRateStaticQuery.class);

	private static String QUERY_ALL = "SELECT last_event_input_message(generate_series, '00:06') FROM generate_series(now()- '00:20'::time,  now(), '00:01'::time)";
	private static String QUERY_LAST = "SELECT last_event_input_message(now(), '00:06')";

	@Override
	public String getId()
	{
		return "inputrate";
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