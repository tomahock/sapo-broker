package pt.com.broker.monitorization.db.queries;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.caudexorigo.jdbc.Db;



public class QueuesRateInformationQuery extends SubjectPredicateQuery
{

//	private static final Logger log = LoggerFactory.getLogger(FaultRateStaticQuery.class);

	private static final String QUEUENAME_PARAM = "queuename";
	private static final String RATE_PARAM = "rate";
	private static Map<String, String> RATE_TYPES = new HashMap<String, String>(5);
	
	static
	{
		RATE_TYPES.put("count", "queue-size");
		RATE_TYPES.put("input", "input-rate");
		RATE_TYPES.put("output", "output-rate");
		RATE_TYPES.put("failed", "failed");
		RATE_TYPES.put("redelivered", "redeliverd");
		RATE_TYPES.put("expired", "expired");
	}
	
	@Override
	public String getId()
	{
		return "queue";
	}
	
	@Override
	public ResultSet getResultSet(Db db, Map<String,List<String>> params)
	{
		String subject = null;
		List<String> list = params.get(QUEUENAME_PARAM);
		if((list != null) && (list.size() == 1))
		{
			subject = list.get(0);
			if(!isValidArgument(subject))
			{
				return null;
			}
			subject = "queue://"  + subject;
		}
		String predicate = null;
		list = params.get(RATE_PARAM);
		if((list != null) && (list.size() == 1))
		{
			predicate = list.get(0);
			predicate = RATE_TYPES.get(predicate);
			if(predicate == null)
			{
				return null;
			}
		}
		
		return db.runRetrievalPreparedStatement(QUERY, subject, predicate);
	}

	private static boolean isValidArgument(String argument)
	{
		//TODO: valid argument
		return true;
	}
}
