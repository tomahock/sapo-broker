package pt.com.broker.monitorization.db.queries;

import java.util.List;
import java.util.Map;

public class QueueInformationRouter
{
	private static String QUEUENAME_PARAM = "queuename";
	private static String AGENTNAME_PARAM = "agentname";
	
	private final static GeneralQueueInfoQuery GENERAL_QUEUE_INFO = new GeneralQueueInfoQuery();
	private final static AllQueuesGeneralInfoQuery ALL_QUEUE_GENERAL_INFO = new AllQueuesGeneralInfoQuery();
	private final static QueueAgentQuery QUEUE_AGENT_INFO = new QueueAgentQuery();
	
	public static String getQueueData(Map<String,List<String>> params)
	{
		List<String> list = null;
		list = params.get(QUEUENAME_PARAM);
		if(list != null && list.size() != 0)
		{
			return GENERAL_QUEUE_INFO.getJsonData(params);
		}
		list = params.get(AGENTNAME_PARAM);
		if(list != null && list.size() != 0)
		{
			return QUEUE_AGENT_INFO.getJsonData(params);
		}
		return ALL_QUEUE_GENERAL_INFO.getJsonData(params);
	}	
}
