package pt.com.broker.monitorization.db.queries;

import java.util.List;
import java.util.Map;

public class SubscriptionsInformationRouter
{
	private static String AGENTNAME_PARAM = "agentname";
	
	private final static TopicsInfoQuery ALL_SUBSCRIPTION_GENERAL_INFO = new TopicsInfoQuery();
	private final static SubscriptionAgentInformationQuery SUBSCRIPTION_AGENT_INFO = new SubscriptionAgentInformationQuery();
	
	public static String getSubscriptionData(Map<String,List<String>> params)
	{
		List<String> list = params.get(AGENTNAME_PARAM);
		if(list != null && list.size() != 0)
		{
			return SUBSCRIPTION_AGENT_INFO.getJsonData(params);
		}
		return ALL_SUBSCRIPTION_GENERAL_INFO.getJsonData(params);
	}	
}
