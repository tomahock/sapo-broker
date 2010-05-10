package pt.com.broker.monitorization.db.queries;

import java.sql.ResultSet;

import pt.com.broker.monitorization.AgentHostname;

public class AgentStatusSnapshotQuery extends ComposedResultQuery
{
	//private static final Logger log = LoggerFactory.getLogger(QueueCountSnapshotQuery.class);

	private static String QUERY = "select raw_data.agent_name, raw_data.object_value as state from raw_data, \n(select agent_name, subject,  max(event_time)  as mtime from raw_data where event_time > (now() - '00:05'::time) AND predicate = 'status' group by agent_name , subject) AS t0\n where raw_data.agent_name= t0.agent_name and raw_data.event_time=t0.mtime and  raw_data.subject=t0.subject and raw_data.predicate='status' order by raw_data.object_value DESC";

	public AgentStatusSnapshotQuery()
	{
		super(QUERY);
	}
	
	public String getId()
	{
		return "agentstatussnapshot";
	}

	protected void getElement(StringBuilder stringBuilder, ResultSet queryResult)  throws Throwable
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
		stringBuilder.append( (queryResult.getDouble(idx++) == 0) ? "Down" : "Ok");
		stringBuilder.append("\"}");		
	}
}



