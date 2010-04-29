package pt.com.broker.monitorization.db.queries;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import org.caudexorigo.jdbc.Db;

public class OutputRateStaticQuery extends StaticQuery
{

//	private static final Logger log = LoggerFactory.getLogger(OutputRateStaticQuery.class);

	private static String QUERY = "SELECT last_event_ouput_message(generate_series) FROM generate_series(now()- time '00:20',  now(), '60 seconds')";

	@Override
	public String getId()
	{
		return "outputrate";
	}

	@Override
	public ResultSet getResultSet(Db db, Map<String,List<String>> params)
	{
		return  db.runRetrievalPreparedStatement(QUERY);
	}
}
