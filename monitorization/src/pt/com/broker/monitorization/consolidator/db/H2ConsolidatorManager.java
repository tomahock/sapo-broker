package pt.com.broker.monitorization.consolidator.db;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.caudexorigo.Shutdown;
import org.caudexorigo.text.StringUtils;
import org.h2.jdbcx.JdbcConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.monitorization.collectors.AgentStatus;
import pt.com.broker.monitorization.collectors.AgentStatusListener;
import pt.com.broker.monitorization.collectors.CollectorManager;
import pt.com.broker.monitorization.collectors.DropboxListener;
import pt.com.broker.monitorization.collectors.FaultListener;
import pt.com.broker.monitorization.collectors.QueueSizeListener;
import pt.com.broker.monitorization.collectors.SubscriptionCountListener;

public class H2ConsolidatorManager
{
	private static final Logger log = LoggerFactory.getLogger(H2ConsolidatorManager.class);

	private static DataSource connPool = null;

	static
	{
		try
		{
			Class.forName("org.h2.Driver");
			connPool = JdbcConnectionPool.create("jdbc:h2:./db/brokerinfo", "", "");
		}
		catch (Throwable t)
		{
			Shutdown.now(t);
		}
	}

	public static void init()
	{
		initDatabaseStructure();

		initConsolidators();
	}

	private static void initDatabaseStructure()
	{
		Connection connection = null;

		connection = H2ConsolidatorManager.getConnection();
		if (connection == null)
		{
			log.error("Unable to get connection");
			return;
		}

		// Porcess script
		String filePath = "./conf/scripts";

		BufferedReader reader = null;
		try
		{
			reader = new BufferedReader(new FileReader(filePath));

			String line;
			while ((line = reader.readLine()) != null)
			{
				try
				{
					if ((!line.startsWith(";")) && StringUtils.isNotBlank(line))
					{
						Statement statement = connection.createStatement();
						statement.execute(line);
					}

				}
				catch (Throwable t)
				{
					log.error("Statement '{}' failed", line);
				}
			}
			reader.close();

		}
		catch (FileNotFoundException fnfe)
		{
			log.error("Failed to open file '{}'", filePath);
			Shutdown.now();
		}
		catch(IOException ioe)
		{
			log.error("Error while reading file '{}'", filePath);
			Shutdown.now();
		}

		if (connection != null)
		{

			try
			{
				connection.close();
			}
			catch (SQLException e)
			{
				log.error("Failed to close database connection");
			}
		}

	}

	private static void initConsolidators()
	{
		// Init subscription count collector
		CollectorManager.getSubscriptionCountCollector().addListener(new SubscriptionCountListener()
		{
			@Override
			public void onUpdate(String agentName, String subscriptionType, String subscriptionName, int count)
			{
				DbSubscription.addSusbscriptionCount(agentName, subscriptionName, subscriptionType, count);
			}
		});

		// Init queue size collector
		CollectorManager.getQueueSizeCollector().addListener(new QueueSizeListener()
		{
			@Override
			public void onUpdate(String agentName, String queueName, int size)
			{
				DbQueue.addQueueCount(agentName, queueName, size);
			}
		});

		// Init faults collector
		CollectorManager.getFaultsCollector().addListener(new FaultListener()
		{
			@Override
			public void onFault(String agentName, String message)
			{
				DbFault.add(agentName, message);
			}
		});

		// Init dropbox collector
		CollectorManager.getDropboxCollector().addListener(new DropboxListener()
		{
			@Override
			public void onUpdate(String agentName, String dropboxLocation, int messages, int goodMessages)
			{
				DbDropbox.addDropboxInfo(agentName, dropboxLocation, messages, goodMessages);
			}
		});

		CollectorManager.getAgentStatusCollector().addListener(new AgentStatusListener()
		{
			@Override
			public void onUpdate(String agentName, AgentStatus status)
			{
				DbAgents.add(agentName, status);
			}
		});

	}

	public static Connection getConnection()
	{
		try
		{
			return connPool.getConnection();
		}
		catch (SQLException e)
		{
			log.error("Failed to obatin a connection", e);
			return null;
		}
	}
}
