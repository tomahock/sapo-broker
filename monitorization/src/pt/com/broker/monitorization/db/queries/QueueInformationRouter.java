package pt.com.broker.monitorization.db.queries;

import java.util.List;
import java.util.Map;

public class QueueInformationRouter
{
	private static String RATE_TYPE = "rate";
	
	private final static QueuesRateInformationQuery QUEUES_RATE_INFO = new QueuesRateInformationQuery();
	private final static GeneralQueueInfoQuery GENERAL_QUEUE_INfo = new GeneralQueueInfoQuery();
	
	public static String getQueueData(Map<String,List<String>> params)
	{
		List<String> list = params.get(RATE_TYPE);
		if(list != null && list.size() != 0)
		{
			return QUEUES_RATE_INFO.getJsonData(params);
		}
		return GENERAL_QUEUE_INfo.getJsonData(params);
	}	
}
