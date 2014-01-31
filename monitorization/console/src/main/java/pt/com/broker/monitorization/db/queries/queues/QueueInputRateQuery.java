package pt.com.broker.monitorization.db.queries.queues;

public class QueueInputRateQuery extends QueueRateQuery
{
	protected static String QUERY_ALL = "SELECT last_event_for_subject_and_predicate(?, 'input-rate', generate_series, '00:01') FROM generate_series(now()- time '00:20',  now(), '00:01'::time)";
	protected static String QUERY_LAST = "SELECT last_event_for_subject_and_predicate(?, 'input-rate', now(), '00:01')";

	public QueueInputRateQuery()
	{
		super(QUERY_ALL, QUERY_LAST);
	}

	@Override
	public String getId()
	{
		return "queueinputrate";
	}
}
