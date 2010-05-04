package pt.com.broker.monitorization.db.queries;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import org.caudexorigo.jdbc.Db;

public class InputRateStaticQuery extends StaticQuery
{
	// private static final Logger log = LoggerFactory.getLogger(InputRateStaticQuery.class);

	private static String QUERY = "SELECT last_event_input_message(generate_series, '00:06') FROM generate_series(now()- '00:20'::time,  now(), '00:01'::time)";

	@Override
	public String getId()
	{
		return "inputrate";
	}

	@Override
	public ResultSet getResultSet(Db db, Map<String, List<String>> params)
	{
		return db.runRetrievalPreparedStatement(QUERY);
	}
}