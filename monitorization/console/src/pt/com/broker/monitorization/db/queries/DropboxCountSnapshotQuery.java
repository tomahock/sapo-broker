package pt.com.broker.monitorization.db.queries;

import java.sql.ResultSet;

import pt.com.broker.monitorization.AgentHostname;

public class DropboxCountSnapshotQuery extends ComposedResultQuery
{
	// private static final Logger log = LoggerFactory.getLogger(QueueCountSnapshotQuery.class);

	private static String QUERY = "SELECT raw_data.agent_name, raw_data.object_value AS COUNT FROM raw_data, \n(SELECT agent_name, subject,  max(event_time)  as mtime from raw_data where event_time > (now() - '00:05'::time) AND subject='dropbox' and predicate = 'count' AND object_value > 0 group by agent_name , subject) AS t0\n WHERE raw_data.agent_name= t0.agent_name AND raw_data.event_time=t0.mtime AND raw_data.subject='dropbox' and  raw_data.predicate='count' AND object_value > 0  order by raw_data.object_value DESC limit 10";

	public DropboxCountSnapshotQuery()
	{
		super(QUERY);
	}

	public String getId()
	{
		return "dropboxcountsnapshot";
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

		stringBuilder.append("\"count\":\"");
		stringBuilder.append(queryResult.getDouble(idx++));
		stringBuilder.append("\"}");
	}
}
