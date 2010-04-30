package pt.com.broker.monitorization.db.queries;

import java.util.List;
import java.util.Map;

public abstract class AgentIntervalQuery extends StaticQuery
{
	private final static String AGENTNAME_PARAM = "agentname";
	
	public static String getAgentName(Map<String, List<String>> params)
	{
		List<String> list = params.get(AGENTNAME_PARAM);
		if( (list != null) && (list.size() == 1) )
		{
			return list.get(0);
		}
		return null;
	}

}