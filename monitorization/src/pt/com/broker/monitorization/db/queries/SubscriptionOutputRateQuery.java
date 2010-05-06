package pt.com.broker.monitorization.db.queries;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import org.caudexorigo.jdbc.Db;

public class SubscriptionOutputRateQuery extends StaticQuery
{

	private final static String SUBSCRIPTION_PARAM = "subscriptionname";
	private final static String QUERY = "SELECT last_event_for_subject_and_predicate(?, 'output-rate', generate_series, '00:01')  FROM generate_series(now()- time '00:20',  now(), '00:01'::time)";
	
	@Override
	public String getId()
	{
		return "subscriptionoutputrate";
	}

	@Override
	protected ResultSet getResultSet(Db db, Map<String, List<String>> params)
	{
		String subscriptionName = getsubscription(params) ;
		if(subscriptionName == null)
		{
			return null;
		}
		return db.runRetrievalPreparedStatement(QUERY, subscriptionName);
	}
	
	
	public static String getsubscription(Map<String, List<String>> params)
	{
		List<String> list = params.get(SUBSCRIPTION_PARAM);
		if( (list != null) && (list.size() == 1) )
		{
			return "topic://"+list.get(0);
		}
		return null;
	}
}
