package pt.com.broker.monitorization.db.queries;

import java.util.List;
import java.util.Map;

public class AgentInformationRouter
{
	private static final AllAgentsGeneralInfoQuery ALL_AGENTS_GENERAL_INFO = new AllAgentsGeneralInfoQuery();
	
	public static String getAgentData(Map<String,List<String>> params)
	{
		return ALL_AGENTS_GENERAL_INFO.getJsonData(params);
	}	
}
