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

import pt.com.broker.monitorization.Utils;
import pt.com.broker.monitorization.collectors.JsonEncodable;
import pt.com.broker.types.NetAction.DestinationType;

public class DbSubscription implements JsonEncodable
{
	private static final Logger log = LoggerFactory.getLogger(DbSubscription.class);
	
	private final String subscription;
	private final String subscriptionType;
	private final int count;
	
	private String agentName;
	private long date;
	
	public DbSubscription(String subscription, String subscriptionType, int count)
	{
		this.subscription = subscription;
		this.subscriptionType = subscriptionType;
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
	
	public String getSubscription()
	{
		return subscription;
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
	
	public String getSubscriptionType()
	{
		return subscriptionType;
	}

	@Override
	public String toJson()
	{
		if (agentName == null)
			return String.format("{\"subscription\":\"%s\",\"subscriptionType\":\"%s\",\"count\":%s}", this.subscription, this.subscriptionType, this.count+ "");
		else
			return String.format("{\"subscription\":\"%s\",\"subscriptionType\":\"%s\",\"count\":%s, \"agentName\":\"%s\", \"date\":\"%s\" }", this.subscription, this.subscriptionType, this.count+ "", agentName, Utils.formatDate(date));
	}
	
	public static void addSusbscriptionCount(String agentName, String subscription, String subscriptionType, int count)
	{
		Connection connection = H2ConsolidatorManager.getConnection();
		try
		{
			if (connection == null)
			{
				log.error("Failed to get a valid connection");
				return;
			}
			PreparedStatement insertStatement = connection.prepareStatement("MERGE INTO SUBSCRIPTIONS ( SUBSCRIPTION , AGENTNAME, SUBSCRIPTIONTYPE, COUNT, TIME ) values (?, ?, ?, ?, ?)");
			insertStatement.setString(1, subscription);
			insertStatement.setString(2, agentName);
			insertStatement.setString(3, subscriptionType);
			insertStatement.setInt(4, count);
			insertStatement.setLong(5, System.currentTimeMillis());

			insertStatement.execute();
			
			PreparedStatement deletetStatement = connection.prepareStatement("delete from subscriptions where time < ?");
			deletetStatement.setLong(1, (System.currentTimeMillis() - (5*60*1000)));
			deletetStatement.execute();

		}
		catch (Throwable t)
		{
			log.error("Failed to update subscription info", t);
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
	
	public static Collection<DbSubscription> getAllSubscriptionCount()
	{
		Collection<DbSubscription> subscritpions = new ArrayList<DbSubscription>();
		Connection connection = H2ConsolidatorManager.getConnection();
		try
		{
			if (connection == null)
			{
				log.error("Failed to get a valid connection");
				return subscritpions;
			}
			PreparedStatement prepareStatement = connection.prepareStatement("select SUBSCRIPTION , AGENTNAME, SUBSCRIPTIONTYPE, COUNT, TIME from SUBSCRIPTIONS");
			ResultSet queryResult = prepareStatement.executeQuery();
			while (queryResult.next())
			{
				DbSubscription dbQueue = new DbSubscription(queryResult.getString(1), queryResult.getString(3), queryResult.getInt(4));
				dbQueue.setDate(queryResult.getLong(5));
				dbQueue.setAgentName(queryResult.getString(2));
				subscritpions.add(dbQueue);
			}
		}
		catch (Throwable t)
		{
			log.error("Failed to get all subscriptions", t);
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
		return subscritpions;
	}
	
	public static Collection<DbSubscription> getAgentSubscriptionCount(String agentName)
	{
		Collection<DbSubscription> subscriptions = new ArrayList<DbSubscription>();
		Connection connection = H2ConsolidatorManager.getConnection();
		try
		{
			if (connection == null)
			{
				log.error("Failed to get a valid connection");
				return subscriptions;
			}
			PreparedStatement prepareStatement = connection.prepareStatement("select SUBSCRIPTION , AGENTNAME, SUBSCRIPTIONTYPE, COUNT, TIME from SUBSCRIPTIONS where AGENTNAME = ?");
			prepareStatement.setString(1, agentName);
			
			ResultSet queryResult = prepareStatement.executeQuery();
			while (queryResult.next())
			{
				DbSubscription dbQueue = new DbSubscription(queryResult.getString(1), queryResult.getString(3), queryResult.getInt(4));
				dbQueue.setDate(queryResult.getLong(5));
				dbQueue.setAgentName(queryResult.getString(2));
				subscriptions.add(dbQueue);
			}
		}
		catch (Throwable t)
		{
			log.error("Failed to get agent subscriptions", t);
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
		return subscriptions;
	}
	
	public static Collection<DbSubscription> getSubscription(String name)
	{
		Collection<DbSubscription> subscriptions = new ArrayList<DbSubscription>();
		Connection connection = H2ConsolidatorManager.getConnection();
		try
		{
			if (connection == null)
			{
				log.error("Failed to get a valid connection");
				return subscriptions;
			}
			PreparedStatement prepareStatement = connection.prepareStatement("select SUBSCRIPTION , AGENTNAME, SUBSCRIPTIONTYPE, COUNT, TIME from SUBSCRIPTIONS where SUBSCRIPTION = ? order by SUBSCRIPTION desc");
			prepareStatement.setString(1, name);
			
			ResultSet queryResult = prepareStatement.executeQuery();
			while (queryResult.next())
			{
				DbSubscription dbQueue = new DbSubscription(queryResult.getString(1), queryResult.getString(3), queryResult.getInt(4));
				dbQueue.setDate(queryResult.getLong(5));
				dbQueue.setAgentName(queryResult.getString(2));
				subscriptions.add(dbQueue);
			}
		}
		catch (Throwable t)
		{
			log.error("Failed to get agent subscriptions", t);
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
		return subscriptions;
	}
	
	public static Collection<DbSubscription> getConsolidatedSubscriptionCount()
	{
		Collection<DbSubscription> subscriptions = new ArrayList<DbSubscription>();
		Connection connection = H2ConsolidatorManager.getConnection();
		try
		{
			if (connection == null)
			{
				log.error("Failed to get a valid connection");
				return subscriptions;
			}
			PreparedStatement prepareStatement = connection.prepareStatement("SELECT subscription, subscriptiontype, SUM(count) as Subscription_Count FROM subscriptions  GROUP BY subscription, subscriptiontype ORDER BY SUM(count) DESC, subscription ASC");
			ResultSet queryResult = prepareStatement.executeQuery();
			while (queryResult.next())
			{
				DbSubscription subscription = new DbSubscription(queryResult.getString(1), queryResult.getString(2), queryResult.getInt(3));
				subscriptions.add(subscription);
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
		return subscriptions;
	}

}
