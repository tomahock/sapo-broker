package pt.com.broker.monitorization.db.queries;

import java.sql.ResultSet;

import org.caudexorigo.jdbc.Db;

public class FaultRateStaticQuery extends StaticQuery
{

//	private static final Logger log = LoggerFactory.getLogger(FaultRateStaticQuery.class);

	private static final String predicate = "rate";
	private static final String subject = "faults";

	private static String QUERY = "SELECT last_event_for_subject_and_predicate(?, ?, generate_series) FROM generate_series(now()- time '00:20',  now(), '60 seconds')";

	@Override
	public String getId()
	{
		return "faultrate";
	}
	
	@Override
	public ResultSet getResultSet(Db db)
	{
		return db.runRetrievalPreparedStatement(QUERY, subject, predicate);
	}
}
