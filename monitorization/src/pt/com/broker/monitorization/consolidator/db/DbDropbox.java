package pt.com.broker.monitorization.consolidator.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbDropbox
{
	private static final Logger log = LoggerFactory.getLogger(DbSubscription.class);

	private final String agentName;
	private final String dropboxLocation;
	private final int messages;
	private final int goodMessages;

	public DbDropbox(String agentName, String dropboxLocation, int messages, int goodMessages)
	{
		this.agentName = agentName;
		this.dropboxLocation = dropboxLocation;
		this.messages = messages;
		this.goodMessages = goodMessages;
	}

	public String getAgentName()
	{
		return agentName;
	}

	public String getDropboxLocation()
	{
		return dropboxLocation;
	}

	public int getMessagesCount()
	{
		return messages;
	}

	public int getGoodMessagesCount()
	{
		return goodMessages;
	}

	public static void addDropboxInfo(String agentName, String dropboxLocation, int messages, int goodMessages)
	{
		Connection connection = H2ConsolidatorManager.getConnection();
		try
		{
			if (connection == null)
			{
				log.error("Failed to get a valid connection");
				return;
			}

			PreparedStatement insertStatement = connection.prepareStatement("merge into dropbox (agentname ,dropboxlocation , messages, goodmessages, time) values (?,?,?,?,?)");
			insertStatement.setString(1, agentName);
			insertStatement.setString(2, dropboxLocation);
			insertStatement.setInt(3, messages);
			insertStatement.setInt(4, goodMessages);
			insertStatement.setLong(5, System.currentTimeMillis());

			insertStatement.execute();

			PreparedStatement deletetStatement = connection.prepareStatement("delete from dropbox where time < ?");
			deletetStatement.setLong(1, (System.currentTimeMillis() - (5 * 60 * 1000)));
			deletetStatement.execute();

		}
		catch (Throwable t)
		{
			log.error("Failed to update dropbox info", t);
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

	public static Collection<DbDropbox> getDropboxes()
	{
		Collection<DbDropbox> dropboxes = new ArrayList<DbDropbox>();
		Connection connection = H2ConsolidatorManager.getConnection();
		try
		{
			if (connection == null)
			{
				log.error("Failed to get a valid connection");
				return dropboxes;
			}
			PreparedStatement prepareStatement = connection.prepareStatement("select agentname ,dropboxlocation , messages, goodmessages from  dropbox");
			ResultSet queryResult = prepareStatement.executeQuery();
			while (queryResult.next())
			{
				DbDropbox dbQueue = new DbDropbox(queryResult.getString(1), queryResult.getString(2), queryResult.getInt(3), queryResult.getInt(4));
				dropboxes.add(dbQueue);
			}
		}
		catch (Throwable t)
		{
			log.error("Failed to get all dropbox information", t);
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
		return dropboxes;
	}

	public static DbDropbox getAgentDropbox(String agentName)
	{
		DbDropbox dropbox = null;
		Connection connection = H2ConsolidatorManager.getConnection();
		try
		{
			if (connection == null)
			{
				log.error("Failed to get a valid connection");
				return new DbDropbox(agentName, "", 0, 0);
			}
			PreparedStatement prepareStatement = connection.prepareStatement("select agentname ,dropboxlocation , messages, goodmessages from  dropbox where agentname = ?");
			prepareStatement.setString(1, agentName);

			ResultSet queryResult = prepareStatement.executeQuery();
			if (queryResult.next())
				dropbox = new DbDropbox(queryResult.getString(1), queryResult.getString(2), queryResult.getInt(3), queryResult.getInt(4));
			else
				dropbox = new DbDropbox(agentName, "", 0, 0);

		}
		catch (Throwable t)
		{
			log.error("Failed to get agent dropboxinfo", t);
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
		return dropbox;
	}
}
