package pt.com.broker.monitorization.db;

import java.io.ByteArrayInputStream;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.caudexorigo.ErrorAnalyser;
import org.caudexorigo.jdbc.Db;
import org.caudexorigo.jdbc.DbExecutor;
import org.caudexorigo.jdbc.DbPool;
import org.caudexorigo.text.DateUtil;
import org.caudexorigo.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import pt.com.broker.monitorization.AgentHostname;

public class FaultsDB
{
	private static Logger log = LoggerFactory.getLogger(FaultsDB.class);

	/*
	 * 
	 * CREATE TABLE IF NOT EXISTS fault(id INT PRIMARY KEY AUTO_INCREMENT, agentName VARCHAR(255) NOT NULL, time TIMESTAMP NOT NULL, message VARCHAR(8192), shortmessage VARCHAR(255));
	 */

	public static class FaultItem
	{
		private final int id;
		private final String agentName;
		private final long time;
		private final String shortMessage;
		private final String message;

		FaultItem(int id, String agentName, long time, String message, String shortMessage)
		{
			this.id = id;
			this.agentName = agentName;
			this.time = time;
			this.shortMessage = shortMessage;
			this.message = message;
		}

		public int getId()
		{
			return id;
		}

		public String getAgentName()
		{
			return agentName;
		}

		public long getTime()
		{
			return time;
		}

		public String getShortMessage()
		{
			return shortMessage;
		}

		public String getMessage()
		{
			return message;
		}

		public String toJson()
		{
			return String.format("{\"id\":\"%s\",\"agentName\":\"%s\",\"agentHostname\":\"%s\",\"time\":\"%s\",\"shortMessage\":\"%s\",\"message\":\"%s\"}", this.id, this.agentName, AgentHostname.get(this.agentName), DateUtil.formatISODate(new Date(time)), shortMessage, message);
		}
	}

	public static class GroupItem
	{
		private final String shortMessage;
		private final int count;

		GroupItem(String shortMessage, int count)
		{
			this.shortMessage = shortMessage;
			this.count = count;
		}

		public int getCount()
		{
			return count;
		}

		public String getShortMessage()
		{
			return shortMessage;
		}

		public String toJson()
		{
			return String.format("{\"shortMessage\":\"%s\",\"count\":\"%s\"}", shortMessage, count);
		}
	}

	public static void add(String agent, Date sampleDate, String message)
	{
		if (log.isDebugEnabled())
		{
			log.debug(String.format("FaultsDB.processItem(%s, %s, %s)", agent, sampleDate, message));
		}

		try
		{

			String ins_sql = ("INSERT INTO fault_data (agent_name, event_time, message, short_message) VALUES (?, ?, ?, ?)");

			ErrorInfo errorInfo = extractShortMessage(message);

			String escapedMsg = StringEscapeUtils.escapeHtml(errorInfo.content.replace("\n", "\\n"));

			String shortMessage = StringEscapeUtils.escapeHtml(errorInfo.shortMessage.replace("\n", "\\n"));

			DbExecutor.runActionPreparedStatement(ins_sql, agent, sampleDate, escapedMsg, shortMessage);
		}
		catch (Throwable t)
		{
			log.error("Failed to insert new information item.", t);
		}

	}

	public static List<FaultItem> getItems(String sqlQuery)
	{
		List<FaultItem> itemsList = new ArrayList<FaultItem>();

		Db db = null;

		try
		{
			db = DbPool.pick();

			ResultSet queryResult = db.runRetrievalStatement(sqlQuery);
			while (queryResult.next())
			{
				int idx = 1;
				FaultItem item = new FaultItem(queryResult.getInt(idx++), queryResult.getString(idx++), queryResult.getTimestamp(idx++).getTime(), queryResult.getString(idx++), queryResult.getString(idx++));
				itemsList.add(item);
			}
		}
		catch (Throwable t)
		{
			log.error(String.format("Failed to get query ('%s') results.", sqlQuery), t);
		}
		finally
		{
			DbPool.release(db);
		}

		return itemsList;
	}

	public static List<GroupItem> getGroupedItems(String sqlQuery)
	{
		List<GroupItem> itemsList = new ArrayList<GroupItem>();

		Db db = null;

		try
		{
			db = DbPool.pick();

			ResultSet queryResult = db.runRetrievalStatement(sqlQuery);
			while (queryResult.next())
			{
				int idx = 1;
				GroupItem item = new GroupItem(queryResult.getString(idx++), queryResult.getInt(idx++));
				itemsList.add(item);
			}
		}
		catch (Throwable t)
		{
			log.error(String.format("Failed to get query ('%s') results.", sqlQuery), t);
		}
		finally
		{
			DbPool.release(db);
		}
		return itemsList;
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

		public java.util.Iterator<Object> getPrefixes(String namespace)
		{
			return null;
		}
	}

	private static class ErrorInfo
	{
		public String content;
		public String shortMessage;

		public ErrorInfo(String shortMessage, String content)
		{
			this.content = content;
			this.shortMessage = shortMessage;
		}
	}

	private static ErrorInfo extractShortMessage(String message)
	{

		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		String shortMessage = "[failed to extract data from Fault message]";
		String content = message;

		try
		{
			DocumentBuilder documentBuilder = docBuilderFactory.newDocumentBuilder();
			Document doc = documentBuilder.parse(new ByteArrayInputStream(message.getBytes()));

			XPath xpath = XPathFactory.newInstance().newXPath();

			xpath.setNamespaceContext(SoapFaultNS.getInstance());

			NodeList n_code = (NodeList) xpath.evaluate("/Envelope/Body/Fault/Reason/Text", doc, XPathConstants.NODESET);

			if (n_code != null && n_code.getLength() > 0)
			{
				Element codeElem = (Element) n_code.item(0);
				shortMessage = codeElem.getTextContent();
			}

			NodeList n_detail = (NodeList) xpath.evaluate("/Envelope/Body/Fault/Detail", doc, XPathConstants.NODESET);

			if (n_detail != null && n_detail.getLength() > 0)
			{
				Element contentElem = (Element) n_detail.item(0);
				content = contentElem.getTextContent();
			}

		}
		catch (Throwable t)
		{
			Throwable r = ErrorAnalyser.findRootCause(t);
			log.error(r.getMessage(), r);
			return new ErrorInfo(r.getMessage(), message);
		}

		return new ErrorInfo(shortMessage, content);
	}
}
