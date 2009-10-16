package pt.com.broker.monitorization.consolidator.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbQueue
{
	private static final Logger log = LoggerFactory.getLogger(DbQueue.class);
	
	private final String name;
	private final int count;
	
	private String agentName;
	private long date;
	
	public DbQueue(String name, int count)
	{
		this.name = name;
		this.count = count;
	}

	public String getAgentName()
	{
		return agentName;
	}
	public void setAgentName(String agentName)
	{
		this.agentName = agentName;
	}

	public String getName()
	{
		return name;
	}

	public int getCount()
	{
		return count;
	}
	
	public void setDate(long date)
	{
		this.date = date;
	}
	
	public String getDate()
	{
		return DateFormat.getInstance().format(new Date(date));
	}
	
	public static void addQueueCount(String agentName, String queueName, int count)
	{
		Connection connection = H2ConsolidatorManager.getConnection();
		try
		{
			if (connection == null)
			{
				log.error("Failed to get a valid connection");
				return;
			}
			PreparedStatement mergeStatement = connection.prepareStatement("MERGE INTO QUEUES (NAME, AGENTNAME, COUNT , TIME ) VALUES(?, ?, ?, ?)");
			mergeStatement.setString(1, queueName);
			mergeStatement.setString(2, agentName);
			mergeStatement.setInt(3, count);
			mergeStatement.setLong(4, System.currentTimeMillis());
			mergeStatement.execute();
			
			PreparedStatement deletetStatement = connection.prepareStatement("delete from queues where time < ?");
			deletetStatement.setLong(1, (System.currentTimeMillis() - (5*60*1000)));
			deletetStatement.execute();
		}
		catch (Throwable t)
		{
			log.error("Failed to get update info about a queue", t);
		}
		if (connection != null)
		{
			try
			{
				connection.close();
			}
			catch (SQLException e)
			{
				log.error("Failed to close db connection", e);
			}
		}
	}
	
	public static Collection<DbQueue> getAllQueueCount()
	{
		Collection<DbQueue> queues = new ArrayList<DbQueue>();
		Connection connection = H2ConsolidatorManager.getConnection();
		try
		{
			if (connection == null)
			{
				log.error("Failed to get a valid connection");
				return queues;
			}
			PreparedStatement prepareStatement = connection.prepareStatement("select name, agentName, count, time from QUEUES");
			ResultSet queryResult = prepareStatement.executeQuery();
			while (queryResult.next())
			{
				DbQueue dbQueue = new DbQueue(queryResult.getString(1), queryResult.getInt(3));
				dbQueue.setAgentName(queryResult.getString(2));
				dbQueue.setDate(queryResult.getLong(4));
				queues.add(dbQueue);
			}
		}
		catch (Throwable t)
		{
			log.error("Failed to get all queues", t);
		}
		if (connection != null)
		{
			try
			{
				connection.close();
			}
			catch (SQLException e)
			{
				log.error("Failed to close db connection", e);
			}
		}
		return queues;
	}
	
	public static Collection<DbQueue> getAgentQueueCount(String agentName)
	{
		Collection<DbQueue> queues = new ArrayList<DbQueue>();
		Connection connection = H2ConsolidatorManager.getConnection();
		try
		{
			if (connection == null)
			{
				log.error("Failed to get a valid connection");
				return queues;
			}
			PreparedStatement prepareStatement = connection.prepareStatement("select name, agentName, count, time from QUEUES where agentName = ?");
			prepareStatement.setString(1, agentName);
			
			ResultSet queryResult = prepareStatement.executeQuery();
			while (queryResult.next())
			{
				DbQueue dbQueue = new DbQueue(queryResult.getString(1), queryResult.getInt(3));
				dbQueue.setAgentName(queryResult.getString(2));
				dbQueue.setDate(queryResult.getLong(4));
				queues.add(dbQueue);
			}
		}
		catch (Throwable t)
		{
			log.error("Failed to get agent queues", t);
		}
		if (connection != null)
		{
			try
			{
				connection.close();
			}
			catch (SQLException e)
			{
				log.error("Failed to close db connection", e);
			}
		}
		return queues;
	}
	
	public static Collection<DbQueue> getConsolidatedQueueCount(int minOccurences)
	{
		Collection<DbQueue> queues = new ArrayList<DbQueue>();
		Connection connection = H2ConsolidatorManager.getConnection();
		try
		{
			if (connection == null)
			{
				log.error("Failed to get a valid connection");
				return queues;
			}
			PreparedStatement prepareStatement = connection.prepareStatement("select * from (SELECT name, SUM(count) as Msg_Count FROM queues  GROUP BY name ORDER BY SUM(count) DESC, name ASC) where Msg_Count > ?");
			prepareStatement.setInt(1, minOccurences);
			ResultSet queryResult = prepareStatement.executeQuery();
			while (queryResult.next())
			{
				DbQueue dbQueue = new DbQueue(queryResult.getString(1), queryResult.getInt(2));
				queues.add(dbQueue);
			}
		}
		catch (Throwable t)
		{
			log.error("Failed to get all queues", t);
		}
		if (connection != null)
		{
			try
			{
				connection.close();
			}
			catch (SQLException e)
			{
				log.error("Failed to close db connection", e);
			}
		}
		return queues;
	}
}
