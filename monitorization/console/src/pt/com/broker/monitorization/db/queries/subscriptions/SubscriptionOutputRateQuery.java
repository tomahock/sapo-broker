package pt.com.broker.monitorization.db.queries.subscriptions;

public class SubscriptionOutputRateQuery extends SubscriptionRateQuery
{
	private static final String QUERY_ALL = "SELECT last_event_for_subject_and_predicate(?, 'output-rate', generate_series, '00:01')  FROM generate_series(now()- time '00:20',  now(), '00:01'::time)";
	private static final String QUERY_LAST = "SELECT last_event_for_subject_and_predicate(?, 'output-rate', now(), '00:01')";

	public SubscriptionOutputRateQuery()
	{
		super(QUERY_ALL, QUERY_LAST);
	}

	public String getId()
	{
		return "subscriptionoutputrate";
	}
}
