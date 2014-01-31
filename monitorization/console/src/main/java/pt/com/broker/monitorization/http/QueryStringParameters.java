package pt.com.broker.monitorization.http;

import java.util.List;
import java.util.Map;

public class QueryStringParameters
{

	public final static String WINDOW_PARAM = "window";
	public final static String WINDOW_PARAM_LAST = "last";
	public final static String WINDOW_PARAM_ALL = "all";

	public final static String SUBSCRIPTION_PARAM = "subscriptionname";

	public final static String AGENTNAME_PARAM = "agentname";

	public static final String QUEUENAME_PARAM = "queuename";

	private static String getParameter(String paramName, Map<String, List<String>> params)
	{
		List<String> list = params.get(paramName);
		if ((list != null) && (list.size() == 1))
		{
			return list.get(0);
		}
		return null;
	}

	public static String getWindowParam(Map<String, List<String>> params)
	{
		return getParameter(WINDOW_PARAM, params);
	}

	public static String getSubscriptionNameParam(Map<String, List<String>> params)
	{
		String subsName = getParameter(SUBSCRIPTION_PARAM, params);
		if (subsName != null)
		{
			return "topic://" + subsName;
		}
		return null;
	}

	public static String getQueueNameParam(Map<String, List<String>> params)
	{
		String subsName = getParameter(QUEUENAME_PARAM, params);
		if (subsName != null)
		{
			return "queue://" + subsName;
		}
		return null;
	}

	public static String getAgentNameParam(Map<String, List<String>> params)
	{
		String agentName = getParameter(AGENTNAME_PARAM, params);
		if (agentName != null)
		{
			return agentName;
		}
		return null;
	}
}
