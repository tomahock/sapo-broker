package pt.com.broker.monitorization.db.queries;

import java.sql.ResultSet;

import pt.com.broker.monitorization.AgentHostname;

public class SysMsgFailedDeliverySnapshotQuery extends ComposedResultQuery
{
	// private static final Logger log = LoggerFactory.getLogger(QueueCountSnapshotQuery.class);

	private static String QUERY = "select raw_data.agent_name, sum(raw_data.object_value) AS sum from raw_data, \n(select agent_name, subject,  max(event_time)  as mtime from raw_data where event_time > (now() - '00:07'::time) AND subject='system-message' and predicate = 'failed-delivery' and object_value > 0  group by agent_name , subject) AS t0\n where raw_data.agent_name= t0.agent_name and raw_data.event_time=t0.mtime and raw_data.subject='system-message' and  raw_data.predicate='failed-delivery' and object_value > 0  GROUP BY raw_data.agent_name order by sum DESC limit 10\n";

	public SysMsgFailedDeliverySnapshotQuery()
	{
		super(QUERY);
	}

	public String getId()
	{
		return "sysmsgfailsnapshot";
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
