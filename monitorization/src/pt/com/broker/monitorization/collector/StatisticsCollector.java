package pt.com.broker.monitorization.collector;

import java.io.ByteArrayInputStream;
import java.util.Date;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.caudexorigo.text.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import pt.com.broker.client.BaseBrokerClient;
import pt.com.broker.client.messaging.BrokerListener;
import pt.com.broker.monitorization.db.StatisticsDB;
import pt.com.broker.types.NetBrokerMessage;
import pt.com.broker.types.NetNotification;
import pt.com.broker.types.NetSubscribe;
import pt.com.broker.types.NetAction.DestinationType;

public class StatisticsCollector
{
	private static Logger log = LoggerFactory.getLogger(StatisticsCollector.class);

	private final String SUBSCRIPTION = "/system/stats/.*";
	private final BaseBrokerClient brokerClient;

	private XPath xpath;

	private XPathExpression agentExpr;
	private XPathExpression dateExpr;

	private XPathExpression itemsExpr;
	private XPathExpression subjectExpr;
	private XPathExpression predicateExpr;
	private XPathExpression valueExpr;
	

	public StatisticsCollector(BaseBrokerClient brokerClient)
	{
		this.brokerClient = brokerClient;

		xpath = XPathFactory.newInstance().newXPath();
		try
		{
			agentExpr = xpath.compile("//@agent-name");
			dateExpr = xpath.compile("//@date");
			
			itemsExpr = xpath.compile("//item");
			subjectExpr = xpath.compile("./@subject");
			predicateExpr = xpath.compile("./@predicate");
			valueExpr = xpath.compile("./@value");
		}
		catch (Throwable t)
		{
			log.error("Failed to initialize XPATH.", t);
			throw new RuntimeException(t);
		}
	}

	public void start()
	{
		BaseBrokerClient bc = getBrokerClient();

		NetSubscribe netSub = new NetSubscribe(getSubscription(), DestinationType.TOPIC);
		try
		{
			bc.addAsyncConsumer(netSub, new BrokerListener()
			{
				@Override
				public boolean isAutoAck()
				{
					return false;
				}

				@Override
				public void onMessage(NetNotification notification)
				{
					messageReceived(notification);
				}
			});
		}
		catch (Throwable t)
		{
			log.error("Failed to initilize subscription.", t);
		}
	}

	private void messageReceived(NetNotification notification)
	{
		String xml = new String(notification.getMessage().getPayload());
		if(log.isDebugEnabled())
		{
			log.debug("Message received: '{}'", xml);
		}
		
		try
		{
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes()));

			String agent = agentExpr.evaluate(document);
			String sampleStart = dateExpr.evaluate(document);
			Date sampleStartDate = DateUtil.parseISODate(sampleStart);

			//process items
			
			NodeList itemsList = (NodeList) itemsExpr.evaluate(document, XPathConstants.NODESET);
			int itemsCount = itemsList.getLength();
			for(int i = 0; i != itemsCount; ++i)
			{
				Node item = itemsList.item(i);
				
				String subject = subjectExpr.evaluate(item);
				String predicate = predicateExpr.evaluate(item);
				String strValue = valueExpr.evaluate(item);
				double value = Double.parseDouble(strValue);
				
				processItem(agent, sampleStartDate, subject, predicate, value);
			}
						
		}
		catch (Throwable t)
		{
			log.error(String.format("Failed to process received message. Error: %s. Message: \n'%s'", t.getMessage(), xml));
		}
	}

	private void processItem(String agent, Date sampleStartDate, String subject, String predicate, double value)
	{
		StatisticsDB.add(agent, sampleStartDate, subject, predicate, value);
	}

	public BaseBrokerClient getBrokerClient()
	{
		return brokerClient;
	}

	public String getSubscription()
	{
		return SUBSCRIPTION;
	}

	public static void main(String[] args)
	{
		try
		{
			String xml = "<stats date='2010-04-06T15:09:25.650Z' agent-name='127.0.0.1:3315'><item subject='queue:///queue/foo' predicate='input-rate' value='123' /><item subject='queue:///queue/foo' predicate='output-rate' value='23' /> 	<item subject='queue:///queue/foo' predicate='subscriptions' value='11223' /> 	<item subject='queue:///queue/foo' predicate='failed' value='123213' /> 	<item subject='queue:///queue/foo' predicate='expired' value='3' /> 	<item subject='queue:///queue/foo' predicate='redelivered' value='23' /> </stats>";

			NetNotification notification = new NetNotification("/system/stats/....", DestinationType.TOPIC, new NetBrokerMessage(xml), "/system/stats/.*");
			
			new StatisticsCollector(null).messageReceived(notification);
			
			System.out.println("END");
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}
	}
}
