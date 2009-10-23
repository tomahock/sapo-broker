package pt.com.broker.monitorization.consolidator.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.caudexorigo.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.monitorization.collectors.JsonEncodable;

public class DbFault implements JsonEncodable
{
	private static final Logger log = LoggerFactory.getLogger(DbFault.class);

	private final String agentName;
	private final String message;
	private final long date;

	public DbFault(String agentName, String message, long date)
	{
		this.agentName = agentName;
		this.message = message;
		this.date = date;
	}

	public String getAgentName()
	{
		return agentName;
	}

	public String getMessage()
	{
		return message;
	}

	public String getDate()
	{
		return DateFormat.getInstance().format(new Date(date));
	}
	
	@Override
	public String toJson()
	{
		return String.format("{\"name\":\"%s\",\"message\":\"%s\",\"date\":\"%s\"}", this.agentName, StringEscapeUtils.escapeHtml(this.message), DateFormat.getInstance().format(new Date(date)));
	}

	public static void add(String agentName, String message)
	{
		Connection connection = H2ConsolidatorManager.getConnection();
		try
		{
			if (connection == null)
			{
				log.error("Failed to get a valid connection");
				return;
			}
			PreparedStatement prepareStatement = connection.prepareStatement("insert into fault (message, agentname, time) values (?, ?, ?)");
			prepareStatement.setString(1, message.substring(0, (message.length() > 4096) ? 4096 : message.length()));
			prepareStatement.setString(2, agentName);
			prepareStatement.setLong(3, System.currentTimeMillis());

			prepareStatement.execute();
			
			PreparedStatement deletetStatement = connection.prepareStatement("delete from fault where time < ?");
			deletetStatement.setLong(1, (System.currentTimeMillis() - (5*60*1000)));
			deletetStatement.execute();
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

	public static Collection<DbFault> getAllFaults()
	{
		Collection<DbFault> faults = new ArrayList<DbFault>();
		Connection connection = H2ConsolidatorManager.getConnection();
		try
		{
			if (connection == null)
			{
				log.error("Failed to get a valid connection");
				return faults;
			}
			PreparedStatement prepareStatement = connection.prepareStatement("select message, agentName, time from Fault order by time desc");
			ResultSet queryResult = prepareStatement.executeQuery();
			while (queryResult.next())
			{
				faults.add(new DbFault(queryResult.getString(2), queryResult.getString(1), queryResult.getLong(3)));
			}
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
		return faults;
	}

	public static Collection<DbFault> getAgentFaults(String agentName)
	{
		Collection<DbFault> faults = new ArrayList<DbFault>();
		Connection connection = H2ConsolidatorManager.getConnection();
		try
		{
			if (connection == null)
			{
				log.error("Failed to get a valid connection");
				return faults;
			}
			PreparedStatement prepareStatement = connection.prepareStatement("select message, agentName, time from Fault where agentName = ? order by time desc");
			prepareStatement.setString(1, agentName);

			ResultSet queryResult = prepareStatement.executeQuery();
			while (queryResult.next())
			{
				faults.add(new DbFault(queryResult.getString(2), queryResult.getString(1), queryResult.getLong(3)));
			}
		}
		catch (Throwable t)
		{
			log.error("Failed to get agent " + agentName + " faults", t);
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
		return faults;
	}

}
