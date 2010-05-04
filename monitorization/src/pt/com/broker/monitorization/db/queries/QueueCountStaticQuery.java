package pt.com.broker.monitorization.db.queries;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import org.caudexorigo.jdbc.Db;

public class QueueCountStaticQuery extends StaticQuery
{
	//private static final Logger log = LoggerFactory.getLogger(QueueCountStaticQuery.class);

	private static final String predicate = "queue-size";

	private static String QUERY = "SELECT last_event_for_predicate(?, generate_series, '00:00:20') FROM generate_series(now()- '00:10'::time,  now(), '00:00:20'::time)";

	@Override
	public String getId()
	{
		return "queuecount";
	}

	@Override
	public ResultSet getResultSet(Db db, Map<String,List<String>> params)
	{
		return db.runRetrievalPreparedStatement(QUERY, predicate);
	}

}
