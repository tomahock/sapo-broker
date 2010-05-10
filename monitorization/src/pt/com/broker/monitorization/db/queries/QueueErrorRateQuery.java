package pt.com.broker.monitorization.db.queries;

public class QueueErrorRateQuery extends QueueRateQuery
{
	protected static String QUERY_ALL = "SELECT last_event_for_subject_and_predicate(?, 'failed-rate', generate_series, '00:06') FROM generate_series(now()- time '00:20',  now(), '00:01'::time)";
	protected static String QUERY_LAST = "SELECT last_event_for_subject_and_predicate(?, 'failed-rate', now(), '00:06')";

	public QueueErrorRateQuery()
	{
		super(QUERY_ALL, QUERY_LAST);
	}
	
	@Override
	public String getId()
	{
		return "queueerrorrate";
	}
}
