package pt.com.broker.monitorization.db.queries;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import org.caudexorigo.jdbc.Db;

public class LatestQueueCountStaticQuery extends StaticQuery
{
	//private static final Logger log = LoggerFactory.getLogger(QueueCountStaticQuery.class);

	private static String QUERY = "SELECT last_event_for_predicate('queue-size', now(), '00:00:20')";

	@Override
	public String getId()
	{
		return "lastqueuecount";
	}

	@Override
	public ResultSet getResultSet(Db db, Map<String,List<String>> params)
	{
		return db.runRetrievalPreparedStatement(QUERY);
	}

}
