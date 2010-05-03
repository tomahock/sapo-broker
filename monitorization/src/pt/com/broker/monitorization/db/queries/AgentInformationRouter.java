package pt.com.broker.monitorization.db.queries;

import java.util.List;
import java.util.Map;

public class AgentInformationRouter
{
	private static final AllAgentsGeneralInfoQuery ALL_AGENTS_GENERAL_INFO = new AllAgentsGeneralInfoQuery();
	private static final AgentMiscInformationQuery AGENT_MISC_INFO = new AgentMiscInformationQuery(); 
	
	private static String AGENTNAME_PARAM = "agentname";
	
	public static String getAgentData(Map<String,List<String>> params)
	{
		List<String> list = params.get(AGENTNAME_PARAM);
		if(list != null && list.size() != 0)
		{
			return AGENT_MISC_INFO.getJsonData(params);
		}
		
		return ALL_AGENTS_GENERAL_INFO.getJsonData(params);
	}
}
