package pt.com.broker.monitorization.db.queries.agents;

import java.sql.ResultSet;

import pt.com.broker.monitorization.AgentHostname;
import pt.com.broker.monitorization.db.queries.ComposedResultQuery;

public class AgentStatusSnapshotQuery extends ComposedResultQuery
{
	// private static final Logger log = LoggerFactory.getLogger(QueueCountSnapshotQuery.class);

	private static String QUERY = "SELECT agents.agent_name, \nlast_event_for_subject_predicate_agent('agent', 'status', agents.agent_name, now(), '00:10') AS state,\nlast_event_for_subject_predicate_agent('queue', 'count', agents.agent_name, now(), '00:10') AS queues\nFROM (SELECT DISTINCT agent_name FROM raw_data WHERE event_time > (now() - '00:10'::time) ) AS agents ORDER BY 3 DESC";

	public AgentStatusSnapshotQuery()
	{
		super(QUERY);
	}

	public String getId()
	{
		return "agentstatussnapshot";
	}

	protected void getElement(StringBuilder stringBuilder, ResultSet queryResult) throws Throwable
	{
		int idx = 1;
		stringBuilder.append("{\"agentName\":\"");
		String agentName = queryResult.getString(idx++);
		stringBuilder.append(agentName);
		stringBuilder.append("\",");

		stringBuilder.append("\"agentHostname\":\"");
		stringBuilder.append(AgentHostname.get(agentName));
		stringBuilder.append("\",");

		stringBuilder.append("\"status\":\"");
		stringBuilder.append((queryResult.getDouble(idx++) == 0) ? "Down" : "Ok");
		stringBuilder.append("\",");

		stringBuilder.append("\"queueCount\":\"");
		stringBuilder.append(queryResult.getDouble(idx++));
		stringBuilder.append("\"}");
	}
}
