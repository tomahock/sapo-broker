package pt.com.broker.monitorization.db.queries;

import java.sql.ResultSet;

import org.caudexorigo.jdbc.Db;

public class InputRateStaticQuery extends StaticQuery
{
	//private static final Logger log = LoggerFactory.getLogger(InputRateStaticQuery.class);

	private static String QUERY = "SELECT last_event_input_message(generate_series) FROM generate_series(now()- time '00:20',  now(), '60 seconds')";

	@Override
	public String getId()
	{
		return "inputrate";
	}

	@Override
	public ResultSet getResultSet(Db db)
	{
		return db.runRetrievalPreparedStatement(QUERY);
	}
}
