package pt.com.broker.monitorization.db.queries;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import org.caudexorigo.jdbc.Db;

public class AgentQueueCountQuery extends AgentIntervalQuery
{

	private final static String QUERY = "SELECT last_event_predicate_for_agent(?, ?, generate_series, '00:01') FROM generate_series(now()- time '00:20',  now(), '00:01'::time)";
	private final static String PREDICATE = "queue-size"; 
	
	@Override
	public String getId()
	{
		return "agentqueuecount";
	}

	@Override
	protected ResultSet getResultSet(Db db, Map<String, List<String>> params)
	{
		String agentName = getAgentName(params) ;
		if(agentName == null)
		{
			return null;
		}
		return db.runRetrievalPreparedStatement(QUERY, PREDICATE, agentName);
	}

}
