package pt.com.broker.monitorization.db.queries.agents;

public class AgentQueueCountQuery extends AgentRateQuery
{

	private final static String QUERY_ALL = "SELECT last_event_predicate_for_agent('queue-size', ?, generate_series, '00:05') FROM generate_series(now()- time '00:20',  now(), '00:01'::time)";
	private final static String QUERY_LAST = "SELECT last_event_predicate_for_agent('queue-size', ?, now(), '00:05')";

	public AgentQueueCountQuery()
	{
		super(QUERY_ALL, QUERY_LAST);
	}

	@Override
	public String getId()
	{
		return "agentqueuecount";
	}
}
