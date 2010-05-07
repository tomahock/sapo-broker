package pt.com.broker.monitorization.db.queries;


public class AgentOutputRateQuery extends AgentRateQuery
{
	private final static String QUERY_ALL = "SELECT last_event_ouput_message_for_agent(?, generate_series, '00:01') FROM generate_series(now()- time '00:20',  now(), '00:01'::time)";
	private final static String QUERY_LAST = "SELECT last_event_ouput_message_for_agent(?, now(), '00:01')";

	public AgentOutputRateQuery()
	{
		super(QUERY_ALL, QUERY_LAST);
	}
	
	@Override
	public String getId()
	{
		return "agentoutputrate";
	}
}
