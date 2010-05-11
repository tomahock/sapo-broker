package pt.com.broker.monitorization.db.queries.agents;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import org.caudexorigo.jdbc.Db;

public class AgentInputRateQuery extends AgentRateQuery
{
	private final static String QUERY_ALL = "SELECT last_event_input_message_for_agent(?, generate_series, '00:01') FROM generate_series(now()- '00:20'::time,  now(), '00:01'::time)";
	private final static String QUERY_LAST = "SELECT last_event_input_message_for_agent(?, now(), '00:01')";
	
	public AgentInputRateQuery()
	{
		super(QUERY_ALL, QUERY_LAST);
	}
	
	@Override
	public String getId()
	{
		return "agentinputrate";
	}
}
