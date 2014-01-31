package pt.com.broker.monitorization.db.queries.agents;

public class AgentFaultsRateQuery extends AgentRateQuery
{

	private final static String QUERY_ALL = "SELECT last_event_for_subject_predicate_agent('faults', 'rate', ?, generate_series, '00:01') FROM generate_series(now()- time '00:20',  now(),  '00:01'::time);";
	private final static String QUERY_LAST = "SELECT last_event_for_subject_predicate_agent('faults', 'rate', ?, now(), '00:01')";

	public AgentFaultsRateQuery()
	{
		super(QUERY_ALL, QUERY_LAST);
	}

	@Override
	public String getId()
	{
		return "agentfaultrate";
	}
}