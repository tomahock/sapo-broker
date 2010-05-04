package pt.com.broker.monitorization.db.queries;

import java.util.List;
import java.util.Map;

public class FaultsInformationRouter
{
	private static String ID_PARAM = "id";
	private static String TYPE_PARAM = "type"; // shortmessage
	private static String AGNETNAME_PARAM = "agentname";
	
	private final static FaultTypeQuery FAULT_TYPE_INFO = new FaultTypeQuery();
	private final static FaultQuery FAULT_INFO = new FaultQuery();
	private final static AgentFaultTypeQuery AGENT_FAULT_TYPE_INFO = new AgentFaultTypeQuery();
	private final static AllFaultTypeQuery ALL_FAULT_TYPE_INFO = new AllFaultTypeQuery();
	
	
	public static String getFaultsInfo(Map<String,List<String>> params)
	{
		List<String> list = params.get(TYPE_PARAM);
		if(list != null && list.size() != 0)
		{
			return FAULT_TYPE_INFO.getJsonData(params);
		}
		list = params.get(ID_PARAM);
		if(list != null && list.size() != 0)
		{
			return FAULT_INFO.getJsonData(params);
		}
		list = params.get(AGNETNAME_PARAM);
		if(list != null && list.size() != 0)
		{
			return AGENT_FAULT_TYPE_INFO.getJsonData(params);
		}
		return ALL_FAULT_TYPE_INFO.getJsonData(params);
	}	
}
