package pt.com.broker.monitorization.consolidator.db;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.caudexorigo.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import pt.com.broker.monitorization.Utils;
import pt.com.broker.monitorization.collectors.JsonEncodable;

public class DbFault implements JsonEncodable
{
	private static final Logger log = LoggerFactory.getLogger(DbFault.class);

	private final int id;
	private final String agentName;
	private String message;
	private final String shortMessage;
	private final long date;

	public DbFault(int id, String agentName, String shortMessage,long date)
	{
		this.id = id;
		this.agentName = agentName;
		this.shortMessage = shortMessage;
		this.date = date;
	}

	public int getId()
	{
		return id;
	}
	
	
	public String getAgentName()
	{
		return agentName;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}

	public String getMessage()
	{
		return message;
	}

	public String getShortMessage()
	{
		return shortMessage;
	}
	
	public String getDate()
	{
		return DateFormat.getInstance().format(new Date(date));
	}
	
	@Override
	public String toJson()
	{
		if(message != null)
			return String.format("{\"id\":%s, \"agentName\":\"%s\",\"message\":\"%s\",\"shortMessage\":\"%s\",\"date\":\"%s\"}", this.id, this.agentName, this.message, this.shortMessage, Utils.formatDate(date));
		return String.format("{\"id\":%s, \"agentName\":\"%s\",\"shortMessage\":\"%s\",\"date\":\"%s\"}", this.id, this.agentName, this.shortMessage, Utils.formatDate(date));
	}

	public static void add(String agentName, String message)
	{
		final int MESSAGE_MAX_SIZE = 8192;
		final int SHORT_MESSAGE_MAX_SIZE = 255;
		
		Connection connection = H2ConsolidatorManager.getConnection();
		try
		{
			if (connection == null)
			{
				log.error("Failed to get a valid connection");
				return;
			}
			PreparedStatement prepareStatement = connection.prepareStatement("insert into fault (message, shortmessage, agentname, time) values (?, ?, ?, ?)");
			
			String escapedMsg =  StringEscapeUtils.escapeHtml(message.replace("\n", "\\n"));
			
			String shortMessage = extractShortMessage(message);
			
			shortMessage = StringEscapeUtils.escapeHtml(shortMessage.replace("\n", "\\n"));
			
			prepareStatement.setString(1, escapedMsg.substring(0, (escapedMsg.length() > MESSAGE_MAX_SIZE) ? MESSAGE_MAX_SIZE : escapedMsg.length()));
			prepareStatement.setString(2, shortMessage.substring(0, (shortMessage.length() > SHORT_MESSAGE_MAX_SIZE) ? SHORT_MESSAGE_MAX_SIZE : shortMessage.length()));
			prepareStatement.setString(3, agentName);
			prepareStatement.setLong(4, System.currentTimeMillis());

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
			PreparedStatement prepareStatement = connection.prepareStatement("select agentName, time, id, shortmessage from Fault order by time desc");
			ResultSet queryResult = prepareStatement.executeQuery();
			while (queryResult.next())
			{
				faults.add(new DbFault(queryResult.getInt(3), queryResult.getString(1), queryResult.getString(4), queryResult.getLong(2)));
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
			PreparedStatement prepareStatement = connection.prepareStatement("select agentName, time, id, shortmessage from Fault where agentName = ? order by time desc");
			prepareStatement.setString(1, agentName);

			ResultSet queryResult = prepareStatement.executeQuery();
			while (queryResult.next())
			{
				faults.add(new DbFault(queryResult.getInt(3), queryResult.getString(1), queryResult.getString(4), queryResult.getLong(2)));
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
	
	public static DbFault getFault(int faultId)
	{
		DbFault fault = null;
		Connection connection = H2ConsolidatorManager.getConnection();
		try
		{
			if (connection == null)
			{
				log.error("Failed to get a valid connection");
				return null;
			}
			PreparedStatement prepareStatement = connection.prepareStatement("select message, agentName, time, id, shortmessage from Fault where id = ?");
			prepareStatement.setInt(1, faultId);
			ResultSet queryResult = prepareStatement.executeQuery();
			while (queryResult.next())
			{
				fault= new DbFault(queryResult.getInt(4), queryResult.getString(2), queryResult.getString(5), queryResult.getLong(3));
				fault.setMessage(queryResult.getString(1));
			}
		}
		catch (Throwable t)
		{
			log.error("Failed to get fault id: " + faultId, t);
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
		return fault;
	}
	
	private static class SoapFaultNS implements NamespaceContext
	{
		private static SoapFaultNS instance = new SoapFaultNS();

		public static SoapFaultNS getInstance()
		{
			return instance;
		}

		public String getNamespaceURI(String prefix)
		{
			if (prefix.equals("soap"))
				return "http://www.w3.org/2003/05/soap-envelope";
			else
				return XMLConstants.NULL_NS_URI;
		}

		public String getPrefix(String namespace)
		{
			if (namespace.equals("http://www.w3.org/2003/05/soap-envelope"))
				return "soap";
			else
				return null;
		}

		public Iterator getPrefixes(String namespace)
		{
			return null;
		}
	}
	
	private static String extractShortMessage(String message)
	{
		
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		String shortMessage = "[failed to extract 'Text' from Fault message]";

		try
		{
			DocumentBuilder documentBuilder = docBuilderFactory.newDocumentBuilder();
			Document doc = documentBuilder.parse(new ByteArrayInputStream(message.getBytes()));
			
			Element documentElement = doc.getDocumentElement();
			
			XPath xpath = XPathFactory.newInstance().newXPath();

			xpath.setNamespaceContext(SoapFaultNS.getInstance());
			
			Element codeElem = (Element) ((NodeList) xpath.evaluate("/Envelope/Body/Fault/Reason/Text", doc, XPathConstants.NODESET)).item(0);
			
			shortMessage = codeElem.getTextContent();
		}
		catch (Throwable t)
		{
			return shortMessage;
		}
		
		return shortMessage;
	}

}
