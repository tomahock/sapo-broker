package pt.com.broker.monitorization.db.queries;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import org.caudexorigo.jdbc.Db;



public class FaultRateStaticQuery extends SubjectPredicateQuery
{

//	private static final Logger log = LoggerFactory.getLogger(FaultRateStaticQuery.class);

	private final String subject = "faults";
	private final String predicate = "rate";
	
	@Override
	public String getId()
	{
		return "faultrate";
	}
	
	@Override
	public ResultSet getResultSet(Db db, Map<String,List<String>> params)
	{
		return db.runRetrievalPreparedStatement(QUERY, subject, predicate);
	}
}
