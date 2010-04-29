package pt.com.broker.monitorization.db.queries;


public abstract class SubjectPredicateQuery extends StaticQuery
{
	protected static String QUERY = "SELECT last_event_for_subject_and_predicate(?, ?, generate_series) FROM generate_series(now()- time '00:20',  now(), '60 seconds')";	
}