package pt.com.broker.monitorization.db.queries;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import org.caudexorigo.jdbc.Db;

public class AgentInputRateQuery extends AgentIntervalQuery
{

	private final static String QUERY = "SELECT last_event_input_message_for_agent(?, generate_series, '00:01') FROM generate_series(now()- '00:20'::time,  now(), '00:01'::time)";
	
	@Override
	public String getId()
	{
		return "agentinputrate";
	}

	@Override
	protected ResultSet getResultSet(Db db, Map<String, List<String>> params)
	{
		String agentName = super.getAgentName(params) ;
		if(agentName == null)
		{
			return null;
		}
		return db.runRetrievalPreparedStatement(QUERY, agentName);
	}

}
