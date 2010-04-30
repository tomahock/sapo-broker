package pt.com.broker.monitorization.db.queries;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import org.caudexorigo.jdbc.Db;

public class AgentOutputRateQuery extends AgentIntervalQuery
{

	private final static String QUERY = "SELECT last_event_ouput_message_for_agent(?, generate_series) FROM generate_series(now()- time '00:20',  now(), '60 seconds')";
	
	@Override
	public String getId()
	{
		return "agentoutputrate";
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
