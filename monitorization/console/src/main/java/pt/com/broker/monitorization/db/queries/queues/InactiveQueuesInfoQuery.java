package pt.com.broker.monitorization.db.queries.queues;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.caudexorigo.jdbc.Db;
import org.caudexorigo.jdbc.DbPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InactiveQueuesInfoQuery
{
	private static final Logger log = LoggerFactory.getLogger(GeneralQueueInfoQuery.class);

	private static String QUERY = "SELECT DISTINCT subject FROM raw_data WHERE predicate = 'queue-listing' AND event_time > (now() - '00:30'::time) AND subject NOT IN\n(\n SELECT raw_data.subject as queue FROM\n	raw_data\n	,(\n		SELECT max(event_time) as m_time, predicate, agent_name\n		FROM raw_data\n		WHERE\n		subject ~ 'queue://.*'\n		AND event_time > (now() - '00:10'::time)\n		GROUP BY predicate, agent_name\n	) last_events\n WHERE\n	raw_data.event_time = last_events.m_time\n	AND raw_data.agent_name = last_events.agent_name\n	AND raw_data.predicate = last_events.predicate\n	AND raw_data.subject ~ 'queue://.*'\n	AND raw_data.predicate <> 'queue-listing'\nGROUP BY raw_data.subject\n)";

	public String getId()
	{
		return "allQueueGeneralInfo";
	}

	private static class QueueInformation
	{
		private final String queueName;

		QueueInformation(String queueName)
		{
			this.queueName = queueName;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((queueName == null) ? 0 : queueName.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			QueueInformation other = (QueueInformation) obj;
			if (queueName == null)
			{
				if (other.queueName != null)
					return false;
			}
			else if (!queueName.equals(other.queueName))
				return false;
			return true;
		}
	}

	public String getJsonData(Map<String, List<String>> params)
	{
		Db db = null;
		StringBuilder sb = new StringBuilder();

		List<QueueInformation> queues = new ArrayList<QueueInformation>(50);

		try
		{
			db = DbPool.pick();

			ResultSet queryResult = getResultSet(db, params);
			if (queryResult == null)
				return "";

			while (queryResult.next())
			{
				int idx = 1;
				String queueName = queryResult.getString(idx++);

				QueueInformation currentQueueInformation = new QueueInformation(queueName);
				queues.add(currentQueueInformation);
			}

			Comparator<QueueInformation> comparator = new Comparator<QueueInformation>()
			{
				@Override
				public int compare(QueueInformation o1, QueueInformation o2)
				{
					return o1.queueName.compareTo(o2.queueName);
				}
			};

			Collections.sort(queues, comparator);

			boolean first = true;
			for (QueueInformation qInfo : queues)
			{
				if (first)
				{
					first = false;
				}
				else
				{
					sb.append(",");
				}
				sb.append("{");
				sb.append("\"queueName\":\"");
				sb.append(qInfo.queueName);
				sb.append("\"");

				sb.append("}");
			}
		}
		catch (Throwable t)
		{
			log.error("Failed to get all queue genral info", t);
		}
		finally
		{
			DbPool.release(db);
		}

		return sb.toString();
	}

	protected ResultSet getResultSet(Db db, Map<String, List<String>> params)
	{
		return db.runRetrievalPreparedStatement(QUERY);
	}
}
