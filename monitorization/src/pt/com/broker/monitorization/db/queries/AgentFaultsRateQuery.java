package pt.com.broker.monitorization.db.queries;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import org.caudexorigo.jdbc.Db;

public class AgentFaultsRateQuery extends AgentIntervalQuery
{

	private final static String QUERY = "SELECT last_event_for_subject_predicate_agent(?, ?, ?, generate_series, '00:01') FROM generate_series(now()- time '00:20',  now(),  '00:01'::time);";
	private final static String SUBJECT = "faults";
	private final static String PREDICATE = "rate";

	@Override
	public String getId()
	{
		return "agentfaultrate";
	}

	@Override
	protected ResultSet getResultSet(Db db, Map<String, List<String>> params)
	{
		String agentName = super.getAgentName(params);
		if (agentName == null)
		{
			return null;
		}
		return db.runRetrievalPreparedStatement(QUERY, SUBJECT, PREDICATE, agentName);
	}
}