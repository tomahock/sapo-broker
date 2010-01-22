package pt.com.broker.monitorization.consolidator.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.monitorization.Utils;
import pt.com.broker.monitorization.collectors.AgentStatus;
import pt.com.broker.monitorization.collectors.JsonEncodable;

public class DbAgents implements JsonEncodable
{
	private static final Logger log = LoggerFactory.getLogger(DbAgents.class);
	
	private final String agentName;
	private final AgentStatus status;
	private final long date;
	
	private String hostname;
	
	public DbAgents(String agentName, AgentStatus status, long date)
	{
		this.agentName = agentName;
		this.status = status;
		this.date = date;
	}

	public String getAgentName()
	{
		return agentName;
	}

	public AgentStatus getStatus()
	{
		return status;
	}

	public long getDate()
	{
		return date;
	}
	
	@Override
	public String toJson()
	{
		if( hostname == null)
		{
			return String.format("{\"agentName\":\"%s\",\"status\":\"%s\",\"date\":\"%s\"}", this.agentName, this.status, Utils.formatDate(date));
		}
		return String.format("{\"agentName\":\"%s\",\"status\":\"%s\",\"date\":\"%s\",\"hostname\":\"%s\"}", this.agentName, this.status, Utils.formatDate(date), this.hostname);
	}
	
	public static void add(String agentName, AgentStatus status)
	{
		Connection connection = H2ConsolidatorManager.getConnection();
		try
		{
			if (connection == null)
			{
				log.error("Failed to get a valid connection");
				return;
			}
			PreparedStatement prepareStatement = connection.prepareStatement("merge into agents (agentname, status, time) values (?, ?, ?)");
			
			
			prepareStatement.setString(1, agentName);
			prepareStatement.setString(2, status.toString());
			prepareStatement.setLong(3, System.currentTimeMillis());

			prepareStatement.execute();
		}
		catch (Throwable t)
		{
			log.error("Failed to get all faults", t);
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
	
	public static Collection<DbAgents> getAllAgents()
	{
		Collection<DbAgents> agents = new ArrayList<DbAgents>();
		Connection connection = H2ConsolidatorManager.getConnection();
		try
		{
			if (connection == null)
			{
				log.error("Failed to get a valid connection");
				return agents;
			}
			PreparedStatement prepareStatement = connection.prepareStatement("select agentName, status, time from agents order by agentName desc");
			ResultSet queryResult = prepareStatement.executeQuery();
			while (queryResult.next())
			{
				DbAgents dbAgent = new DbAgents(queryResult.getString(1), AgentStatus.valueOf(queryResult.getString(2)),queryResult.getLong(3));
				agents.add(dbAgent);
			}
		}
		catch (Throwable t)
		{
			log.error("Failed to get all agents", t);
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
		return agents;
	}
	
	public static Collection<DbAgents> getAgentsWithStatus(AgentStatus status)
	{
		Collection<DbAgents> agents = new ArrayList<DbAgents>();
		Connection connection = H2ConsolidatorManager.getConnection();
		try
		{
			if (connection == null)
			{
				log.error("Failed to get a valid connection");
				return agents;
			}
			PreparedStatement prepareStatement = connection.prepareStatement("select agentName, status, time from agents where status = ? order by agentName desc ");
			prepareStatement.setString(1, status.toString());
			ResultSet queryResult = prepareStatement.executeQuery();
			while (queryResult.next())
			{
				DbAgents dbAgent = new DbAgents(queryResult.getString(1), AgentStatus.valueOf(queryResult.getString(2)),queryResult.getLong(3));
				agents.add(dbAgent);
			}
		}
		catch (Throwable t)
		{
			log.error("Failed to get agents with status " + status.toString(), t);
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
		return agents;
	}
	
	public static Collection<DbAgents> getAgentStatus(String agentname)
	{
		Collection<DbAgents> agents = new ArrayList<DbAgents>();
		Connection connection = H2ConsolidatorManager.getConnection();
		try
		{
			if (connection == null)
			{
				log.error("Failed to get a valid connection");
				return agents;
			}
			PreparedStatement prepareStatement = connection.prepareStatement("select agentName, status, time from agents where agentName = ? ");
			prepareStatement.setString(1, agentname);
			ResultSet queryResult = prepareStatement.executeQuery();
			while (queryResult.next())
			{
				DbAgents dbAgent = new DbAgents(queryResult.getString(1), AgentStatus.valueOf(queryResult.getString(2)),queryResult.getLong(3));
				agents.add(dbAgent);
			}
		}
		catch (Throwable t)
		{
			log.error("Failed to get agent: " + agentname, t);
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
		return agents;
	}

	public void setHostname(String hostname)
	{
		this.hostname = hostname;
	}

	public String getHostname()
	{
		return hostname;
	}
}
