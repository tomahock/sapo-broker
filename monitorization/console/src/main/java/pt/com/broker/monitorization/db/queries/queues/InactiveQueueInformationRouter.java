package pt.com.broker.monitorization.db.queries.queues;

import pt.com.broker.monitorization.db.queries.QueryDataProvider;

import java.util.List;
import java.util.Map;

public class InactiveQueueInformationRouter implements QueryDataProvider
{
	private final static InactiveQueuesInfoQuery QUEUE_INFO = new InactiveQueuesInfoQuery();

	private static final String TYPE = "inactivequeue";

	public String getData(String queryType, Map<String, List<String>> params)
	{
		return QUEUE_INFO.getJsonData(params);
	}

	@Override
	public String getType()
	{
		return TYPE;
	}
}
