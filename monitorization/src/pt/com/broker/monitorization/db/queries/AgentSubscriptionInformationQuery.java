package pt.com.broker.monitorization.db.queries;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import org.caudexorigo.jdbc.Db;

public class AgentSubscriptionInformationQuery
{
	private static final String QUERY = "SELECT \n	subscriptions.subject AS subscription\n	, last_event_for_subject_predicate_agent(subscriptions.subject, 'subscriptions', ?, now()) AS queuesize\nFROM (SELECT DISTINCT subject FROM raw_data WHERE agent_name = ? and predicate = 'subscriptions' AND event_time > (now() - time '00:25')) AS subscriptions\nOrder BY 2 DESC";
	
	private final static String AGENTNAME_PARAM = "agentname";
	
	public String getJsonData(Map<String,List<String>> params)
	{
		//TODO: this..
		return null;
	}
	
	protected ResultSet getResultSet(Db db, Map<String, List<String>> params)
	{
		String agentName = getAgentName(params) ;
		if(agentName == null)
		{
			return null;
		}
		return db.runRetrievalPreparedStatement(QUERY, AGENTNAME_PARAM, AGENTNAME_PARAM);
	}
	
	public static String getAgentName(Map<String, List<String>> params)
	{
		List<String> list = params.get(AGENTNAME_PARAM);
		if( (list != null) && (list.size() == 1) )
		{
			return list.get(0);
		}
		return null;
	}
}
