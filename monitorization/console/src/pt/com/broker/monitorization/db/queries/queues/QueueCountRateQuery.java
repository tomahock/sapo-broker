package pt.com.broker.monitorization.db.queries.queues;

public class QueueCountRateQuery extends QueueRateQuery
{
	protected static String QUERY_ALL = "SELECT last_event_for_subject_and_predicate(?, 'queue-size', generate_series, '00:01') FROM generate_series(now()- time '00:20',  now(), '00:01'::time)";
	protected static String QUERY_LAST = "SELECT last_event_for_subject_and_predicate(?, 'queue-size', now(), '00:01')";

	public QueueCountRateQuery()
	{
		super(QUERY_ALL, QUERY_LAST);
	}

	@Override
	public String getId()
	{
		return "queuecountrate";
	}
}
