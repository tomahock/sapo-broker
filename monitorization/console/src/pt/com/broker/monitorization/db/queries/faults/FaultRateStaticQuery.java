package pt.com.broker.monitorization.db.queries.faults;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import org.caudexorigo.jdbc.Db;

import pt.com.broker.monitorization.db.queries.StaticQuery;
import pt.com.broker.monitorization.http.QueryStringParameters;

public class FaultRateStaticQuery extends StaticQuery
{

	// private static final Logger log = LoggerFactory.getLogger(FaultRateStaticQuery.class);

	protected static String QUERY_ALL = "SELECT last_event_for_subject_and_predicate(?, ?, generate_series, '00:06') FROM generate_series(now()- time '00:20',  now(), '00:01'::time)";
	protected static String QUERY_LAST = "SELECT last_event_for_subject_and_predicate(?, ?, now(), '00:06')";

	private final String subject = "faults";
	private final String predicate = "rate";

	@Override
	public String getId()
	{
		return "faultrate";
	}

	@Override
	public ResultSet getResultSet(Db db, Map<String, List<String>> params)
	{
		String windowParam = QueryStringParameters.getWindowParam(params);
		if (windowParam != null)
		{
			if (windowParam.equals(QueryStringParameters.WINDOW_PARAM_ALL))
			{
				return db.runRetrievalPreparedStatement(QUERY_ALL, subject, predicate);
			}
			else if (windowParam.equals(QueryStringParameters.WINDOW_PARAM_LAST))
			{
				return db.runRetrievalPreparedStatement(QUERY_LAST, subject, predicate);
			}
			return null;
		}
		return db.runRetrievalPreparedStatement(QUERY_ALL, subject, predicate);
	}
}
