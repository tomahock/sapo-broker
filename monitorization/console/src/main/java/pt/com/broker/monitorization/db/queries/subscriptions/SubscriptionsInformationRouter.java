package pt.com.broker.monitorization.db.queries.subscriptions;

import java.util.List;
import java.util.Map;

import pt.com.broker.monitorization.db.queries.QueryDataProvider;

public class SubscriptionsInformationRouter implements QueryDataProvider
{
	private static String AGENTNAME_PARAM = "agentname";
	private static String SUBSCRIPTIONNAME_PARAM = "subscriptionname";

	private static final String TYPE = "subscription";

	private final static AllSubscriptionInfoQuery ALL_SUBSCRIPTION_GENERAL_INFO = new AllSubscriptionInfoQuery();
	private final static SubscriptionAgentInformationQuery SUBSCRIPTION_AGENT_INFO = new SubscriptionAgentInformationQuery();
	private final static GeneralSubscriptionInfoQuery GENERAL_SUBSCRIPTION_INFO = new GeneralSubscriptionInfoQuery();

	public String getData(String queryType, Map<String, List<String>> params)
	{
		List<String> list = params.get(AGENTNAME_PARAM);
		if (list != null && list.size() != 0)
		{
			return SUBSCRIPTION_AGENT_INFO.getJsonData(params);
		}
		list = params.get(SUBSCRIPTIONNAME_PARAM);
		if (list != null && list.size() != 0)
		{
			return GENERAL_SUBSCRIPTION_INFO.getJsonData(params);
		}

		return ALL_SUBSCRIPTION_GENERAL_INFO.getJsonData(params);
	}

	@Override
	public String getType()
	{
		return TYPE;
	}
}
