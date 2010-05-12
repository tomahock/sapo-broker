package pt.com.broker.monitorization.db;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.caudexorigo.jdbc.Db;
import org.caudexorigo.jdbc.DbExecutor;
import org.caudexorigo.jdbc.DbPool;
import org.caudexorigo.text.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.monitorization.AgentHostname;

public class StatisticsDB
{
	private static Logger log = LoggerFactory.getLogger(StatisticsDB.class);

	/*
	 * CREATE TABLE IF NOT EXISTS statistics(agentname VARCHAR(255) NOT NULL, time TIMESTAMP NOT NULL, subject VARCHAR(256) NOT NULL, predicate VARCHAR(255) NOT NULL, value DOUBLE NOT NULL);
	 */

	public static class StatisticsItem
	{
		private final String agentName;
		private final long time;
		private final String subject;
		private final String predicate;
		private final double value;

		StatisticsItem(String agentName, long time, String subject, String predicate, double value)
		{
			this.agentName = agentName;
			this.time = time;
			this.subject = subject;
			this.predicate = predicate;
			this.value = value;

		}

		public String getAgentName()
		{
			return agentName;
		}

		public long getTime()
		{
			return time;
		}

		public String getSubject()
		{
			return subject;
		}

		public String getPredicate()
		{
			return predicate;
		}

		public double getValue()
		{
			return value;
		}

		public String toJson()
		{
			return String.format("{\"agentName\":\"%s\",\"agentHostname\":\"%s\",\"time\":\"%s\",\"subject\":\"%s\",\"predicate\":\"%s\",\"value\":\"%s\"}", this.agentName, AgentHostname.get(agentName), DateUtil.formatISODate(new Date(time)), subject, predicate, value);
		}
	}

	public static void add(String agent, Date sampleDate, String subject, String predicate, double value)
	{
		if (log.isDebugEnabled())
		{
			log.debug(String.format("StatisticsCollector.processItem(%s, %s, %s, %s, %s)", agent, sampleDate, subject, predicate, value));
		}

		try
		{
			DbExecutor.runActionPreparedStatement("INSERT INTO raw_data (agent_name, event_time, subject, predicate, object_value) VALUES (?, ?, ?, ?, ?)", agent, sampleDate, subject, predicate, value);
		}
		catch (Throwable t)
		{
			log.error("Failed to insert new information item.", t);
		}
	}

	public static List<StatisticsItem> getItems(String sqlQuery)
	{
		List<StatisticsItem> itemsList = new ArrayList<StatisticsItem>();

		Db db = null;

		try
		{
			db = DbPool.pick();

			ResultSet queryResult = db.runRetrievalStatement(sqlQuery);

			while (queryResult.next())
			{
				int idx = 1;
				StatisticsItem item = new StatisticsItem(queryResult.getString(idx++), queryResult.getTimestamp(idx++).getTime(), queryResult.getString(idx++), queryResult.getString(idx++), queryResult.getDouble(idx++));
				itemsList.add(item);
			}
		}
		catch (Throwable t)
		{
			log.error(String.format("Failed to get query ('%s') results.", sqlQuery), t);
		}
		finally
		{
			DbPool.release(db);
		}
		return itemsList;
	}
}
