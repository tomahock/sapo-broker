package pt.com.broker.monitorization.db.queries.queues;

import java.util.List;
import java.util.Map;

import pt.com.broker.monitorization.db.queries.QueryDataProvider;
import pt.com.broker.monitorization.http.QueryStringParameters;

public class QueueInformationRouter implements QueryDataProvider
{
	private final static GeneralQueueInfoQuery GENERAL_QUEUE_INFO = new GeneralQueueInfoQuery();
	private final static AllQueuesGeneralInfoQuery ALL_QUEUE_GENERAL_INFO = new AllQueuesGeneralInfoQuery();
	private final static QueueAgentQuery QUEUE_AGENT_INFO = new QueueAgentQuery();

	private static final String TYPE = "queue";

	public String getData(String queryType, Map<String, List<String>> params)
	{
		List<String> list = null;
		list = params.get(QueryStringParameters.QUEUENAME_PARAM);
		if (list != null && list.size() != 0)
		{
			return GENERAL_QUEUE_INFO.getJsonData(params);
		}
		list = params.get(QueryStringParameters.AGENTNAME_PARAM);
		if (list != null && list.size() != 0)
		{
			return QUEUE_AGENT_INFO.getJsonData(params);
		}
		return ALL_QUEUE_GENERAL_INFO.getJsonData(params);
	}

	@Override
	public String getType()
	{
		return TYPE;
	}
}
