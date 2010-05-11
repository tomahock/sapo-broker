package pt.com.broker.monitorization.db.queries.faults;

import java.util.List;
import java.util.Map;

import pt.com.broker.monitorization.db.queries.QueryDataProvider;
import pt.com.broker.monitorization.db.queries.agents.AgentFaultTypeQuery;
import pt.com.broker.monitorization.http.QueryStringParameters;

public class FaultsInformationRouter implements QueryDataProvider
{
	private static String ID_PARAM = "id";
	private static String TYPE_PARAM = "type"; // shortmessage

	private final static FaultTypeQuery FAULT_TYPE_INFO = new FaultTypeQuery();
	private final static FaultQuery FAULT_INFO = new FaultQuery();
	private final static AgentFaultTypeQuery AGENT_FAULT_TYPE_INFO = new AgentFaultTypeQuery();
	private final static AllFaultTypeQuery ALL_FAULT_TYPE_INFO = new AllFaultTypeQuery();

	private static final String TYPE = "faults";

	public String getData(String queryType, Map<String, List<String>> params)
	{
		List<String> list = params.get(TYPE_PARAM);
		if (list != null && list.size() != 0)
		{
			return FAULT_TYPE_INFO.getJsonData(params);
		}
		list = params.get(ID_PARAM);
		if (list != null && list.size() != 0)
		{
			return FAULT_INFO.getJsonData(params);
		}
		list = params.get(QueryStringParameters.AGENTNAME_PARAM);
		if (list != null && list.size() != 0)
		{
			return AGENT_FAULT_TYPE_INFO.getJsonData(params);
		}
		return ALL_FAULT_TYPE_INFO.getJsonData(params);
	}

	@Override
	public String getType()
	{
		return TYPE;
	}
}
