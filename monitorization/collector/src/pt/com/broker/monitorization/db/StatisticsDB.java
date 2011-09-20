package pt.com.broker.monitorization.db;

import java.util.Date;

import org.caudexorigo.jdbc.DbExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatisticsDB
{
	private static Logger log = LoggerFactory.getLogger(StatisticsDB.class);

	/*
	 * CREATE TABLE IF NOT EXISTS statistics(agentname VARCHAR(255) NOT NULL, time TIMESTAMP NOT NULL, subject VARCHAR(256) NOT NULL, predicate VARCHAR(255) NOT NULL, value DOUBLE NOT NULL);
	 */

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
}
