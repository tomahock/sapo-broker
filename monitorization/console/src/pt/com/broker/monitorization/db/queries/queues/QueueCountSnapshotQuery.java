package pt.com.broker.monitorization.db.queries.queues;

import java.sql.ResultSet;

import pt.com.broker.monitorization.db.queries.ComposedResultQuery;

public class QueueCountSnapshotQuery extends ComposedResultQuery
{
	// private static final Logger log = LoggerFactory.getLogger(QueueCountSnapshotQuery.class);

	private static String QUERY = "SELECT raw_data.subject, sum(raw_data.object_value) AS sum FROM raw_data, \n(SELECT agent_name, subject,  max(event_time)  AS mtime FROM raw_data WHERE event_time > (now() - '00:01'::time) AND predicate = 'queue-size' GROUP BY agent_name , subject) AS t0\nWHERE raw_data.agent_name= t0.agent_name and raw_data.event_time=t0.mtime and  raw_data.subject=t0.subject and raw_data.predicate='queue-size' AND object_value > 0  GROUP BY raw_data.subject ORDER BY sum DESC LIMIT 10";

	public QueueCountSnapshotQuery()
	{
		super(QUERY);
	}

	public String getId()
	{
		return "queuecountsnapshot";
	}

	protected void getElement(StringBuilder stringBuilder, ResultSet queryResult) throws Throwable
	{
		int idx = 1;
		stringBuilder.append("{\"queueName\":\"");
		stringBuilder.append(queryResult.getString(idx++));
		stringBuilder.append("\",");

		stringBuilder.append("\"count\":\"");
		stringBuilder.append(queryResult.getDouble(idx++));
		stringBuilder.append("\"}");
	}
}
