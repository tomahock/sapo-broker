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

public class AllQueuesGeneralInfoQuery
{
	private static final Logger log = LoggerFactory.getLogger(GeneralQueueInfoQuery.class);

	private static String QUERY = "SELECT raw_data.subject, last_events.predicate, \nCOALESCE(SUM(raw_data.object_value), 0) FROM\n	raw_data\n	,(\n		SELECT max(event_time) as m_time, predicate, agent_name\n		FROM raw_data\n		WHERE\n		subject ~ 'queue://.*'\n		AND event_time > (now() - '00:10'::time)\n		GROUP BY predicate, agent_name\n	) last_events\nWHERE\n	raw_data.event_time = last_events.m_time\n	AND raw_data.agent_name = last_events.agent_name\n	AND raw_data.predicate = last_events.predicate\n	AND raw_data.subject ~ 'queue://.*'\n	AND raw_data.predicate <> 'queue-listing'\nGROUP BY raw_data.subject, last_events.predicate\nORDER BY raw_data.subject, last_events.predicate";

	public String getId()
	{
		return "allQueueGeneralInfo";
	}

	private static class QueueInformation
	{
		private final String queueName;

		private double queueSize;
		private double inputRate;
		private double outputRate;
		private double expiredRate;
		private double failedRate;
		private double redeliveredRate;
		private double subscriptions;

		QueueInformation(String queueName)
		{
			this.queueName = queueName;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			long temp;
			temp = Double.doubleToLongBits(expiredRate);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(failedRate);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(inputRate);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(outputRate);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			result = prime * result + ((queueName == null) ? 0 : queueName.hashCode());
			temp = Double.doubleToLongBits(queueSize);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(redeliveredRate);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(subscriptions);
			result = prime * result + (int) (temp ^ (temp >>> 32));
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
			if (Double.doubleToLongBits(expiredRate) != Double.doubleToLongBits(other.expiredRate))
				return false;
			if (Double.doubleToLongBits(failedRate) != Double.doubleToLongBits(other.failedRate))
				return false;
			if (Double.doubleToLongBits(inputRate) != Double.doubleToLongBits(other.inputRate))
				return false;
			if (Double.doubleToLongBits(outputRate) != Double.doubleToLongBits(other.outputRate))
				return false;
			if (queueName == null)
			{
				if (other.queueName != null)
					return false;
			}
			else if (!queueName.equals(other.queueName))
				return false;
			if (Double.doubleToLongBits(queueSize) != Double.doubleToLongBits(other.queueSize))
				return false;
			if (Double.doubleToLongBits(redeliveredRate) != Double.doubleToLongBits(other.redeliveredRate))
				return false;
			if (Double.doubleToLongBits(subscriptions) != Double.doubleToLongBits(other.subscriptions))
				return false;
			return true;
		}

	}

	public String getJsonData(Map<String, List<String>> params)
	{
		Db db = null;
		StringBuilder sb = new StringBuilder();

		List<QueueInformation> queues = new ArrayList<QueueInformation>(100);

		try
		{
			db = DbPool.pick();

			ResultSet queryResult = getResultSet(db, params);
			if (queryResult == null)
				return "";

			QueueInformation currentQueueInformation = null;

			while (queryResult.next())
			{
				int idx = 1;
				String queueName = queryResult.getString(idx++);
				String predicate = queryResult.getString(idx++);
				double value = queryResult.getDouble(idx++);

				if (currentQueueInformation == null)
				{
					currentQueueInformation = new QueueInformation(queueName);
				}
				else if (!currentQueueInformation.queueName.equals(queueName))
				{
					queues.add(currentQueueInformation);
					currentQueueInformation = new QueueInformation(queueName);
				}

				if (predicate.equals("expired-rate"))
				{
					currentQueueInformation.expiredRate = value;
				}
				else if (predicate.equals("failed-rate"))
				{
					currentQueueInformation.failedRate = value;
				}
				else if (predicate.equals("input-rate"))
				{
					currentQueueInformation.inputRate = value;
				}
				else if (predicate.equals("output-rate"))
				{
					currentQueueInformation.outputRate = value;
				}
				else if (predicate.equals("redelivered-rate"))
				{
					currentQueueInformation.redeliveredRate = value;
				}
				else if (predicate.equals("subscriptions"))
				{
					currentQueueInformation.subscriptions = value;
				}
				else if (predicate.equals("queue-size"))
				{
					currentQueueInformation.queueSize = value;
				}
				else
				{
					log.error("Unexpected predicate type : '{}'", predicate);
				}

			}

			if (currentQueueInformation != null)
			{
				queues.add(currentQueueInformation); // queue information is just added on new queue processing. Query has ended but the last queue hasn't been added.
			}

			Comparator<QueueInformation> comparator = new Comparator<QueueInformation>()
			{
				@Override
				public int compare(QueueInformation o1, QueueInformation o2)
				{
					// If bigger say it's smaller... (DESC ordering)
					double dif = o2.queueSize - o1.queueSize;
					if (dif != 0)
						return (int) dif;
					dif = (o2.inputRate + o2.outputRate) - (o1.inputRate + o1.outputRate);
					if (dif != 0d)
					{
						return dif > 0 ? 1 : -1;
					}
					dif = o2.subscriptions - o1.inputRate;
					return (int) dif;
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
				sb.append("\",");

				sb.append("\"queueSize\":\"");
				sb.append(qInfo.queueSize);
				sb.append("\",");

				sb.append("\"inputRate\":\"");
				sb.append(qInfo.inputRate);
				sb.append("\",");

				sb.append("\"outputRate\":\"");
				sb.append(qInfo.outputRate);
				sb.append("\",");

				sb.append("\"failedRate\":\"");
				sb.append(qInfo.failedRate);
				sb.append("\",");

				sb.append("\"expiredRate\":\"");
				sb.append(qInfo.expiredRate);
				sb.append("\",");

				sb.append("\"redeliveredRate\":\"");
				sb.append(qInfo.redeliveredRate);
				sb.append("\",");

				sb.append("\"subscriptions\":\"");
				sb.append(qInfo.subscriptions);
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
