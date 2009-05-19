package pt.com.broker.auth.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class JdbcRoles
{
	private static final String postgreSQLDriver = "org.postgresql.Driver";

	private static final String databaseLocation = "localhost";
	private static final String databaseName = "BROKER_ROLES";
	private static final String databaseUsername = "roles_user";
	private static final String databasePassword = "roles_user";

	private static volatile Connection connection = null;

	private static boolean init()
	{
		try
		{
			Class.forName(postgreSQLDriver);
		}
		catch (ClassNotFoundException cnfe)
		{
			return false;
		}
		return true;
	}

	private static Connection getConnection()
	{
		Connection conn = null;

		try
		{
			String databaseConnectionString = String.format("jdbc:postgresql://%s/%s", databaseLocation, databaseName);
			conn = DriverManager.getConnection(databaseConnectionString, databaseUsername, databasePassword);
		}
		catch (SQLException se)
		{
		}
		return conn;
	}

	static
	{
		if (init())
		{
			connection = getConnection();
		}

	}

	private static Statement getStatement()
	{
		if (connection != null)
		{
			try
			{
				return connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			}
			catch (SQLException e)
			{
				return null;
			}
		}
		return null;
	}

	public static boolean validate(String username, String password) throws Throwable
	{
		Statement statement = getStatement();
		if (statement == null)
			throw new Exception("validate: Failed to obtain a valid database statement");
		// Note: this is not the right way to do it - SQL Injection!! Have you
		// heard of it?
		String query = String.format("select count(*) from users where (user_name= '%s' AND user_password= '%s')", username, password);
		ResultSet resultSet = statement.executeQuery(query);
		resultSet.next();

		int count = resultSet.getInt(1);

		resultSet.close();
		statement.close();

		return count == 1;
	}

	public static List<String> getRoles(String username) throws Throwable
	{
		Statement statement = getStatement();
		if (statement == null)
			throw new Exception("getRoles: Failed to obtain a valid database statement");
		List<String> roles = new ArrayList<String>();
		// Note: this is not the right way to do it - SQL Injection!! Have you
		// heard of it?
		String query = String.format("select user_role from users join user_roles on (users.user_id = user_roles.user_id) where (users.user_name = '%s')", username);
		ResultSet resultSet = statement.executeQuery(query);

		while (resultSet.next())
		{
			roles.add(resultSet.getString(1).trim());
		}

		resultSet.close();
		statement.close();

		return roles;
	}
}
