package pt.com.broker.auth.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * JdbcRoles deals with database access.
 * 
 */

public class JdbcRoles
{
	private final String driverClass;
	private final String databaseUrl;
	private final String databaseUsername;
	private final String databasePassword;

	public JdbcRoles(String driverClass, String databaseUrl, String databaseUsername, String databasePassword)
	{
		this.driverClass = driverClass;
		this.databaseUrl = databaseUrl;
		this.databaseUsername = databaseUsername;
		this.databasePassword = databasePassword;
	}

	public boolean init()
	{
		try
		{
			Class.forName(driverClass);
		}
		catch (ClassNotFoundException cnfe)
		{
			return false;
		}
		return true;
	}

	private Connection getConnection() throws Throwable
	{
		Connection conn = null;

		conn = DriverManager.getConnection(databaseUrl, databaseUsername, databasePassword);

		return conn;

	}

	public boolean validate(String username, String password) throws Throwable
	{
		Connection connection = getConnection();

		PreparedStatement statement = connection.prepareStatement("select count(*) from users where (username= ? AND password= ?)");

		statement.setString(1, username);
		statement.setString(2, password);

		ResultSet resultSet = statement.executeQuery();
		resultSet.next();
		
		int count = resultSet.getInt(1);

		System.out.println("count: " + count);

		resultSet.close();
		statement.close();

		connection.close();

		return count == 1;
	}

	public List<String> getRoles(String username) throws Throwable
	{
		System.out.println("JdbcRoles.getRoles()");
		
		
		Connection connection = getConnection();

		PreparedStatement statement = connection.prepareStatement("select role from users join roles on (users.username = roles.username) where (users.username = ?)");

		statement.setString(1, username);

		ResultSet resultSet = statement.executeQuery();

		List<String> roles = new ArrayList<String>();

		while (resultSet.next())
		{
			String role = resultSet.getString(1).trim();
			System.out.println("role: " + role);
			roles.add(role);
		}

		resultSet.close();
		statement.close();
		connection.close();

		return roles;
	}
}
