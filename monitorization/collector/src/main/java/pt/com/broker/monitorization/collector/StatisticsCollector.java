package pt.com.broker.monitorization.collector;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.Date;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import org.caudexorigo.text.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.client.BrokerClient;
import pt.com.broker.client.messaging.BrokerListener;
import pt.com.broker.monitorization.db.StatisticsDB;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.broker.types.NetBrokerMessage;
import pt.com.broker.types.NetNotification;
import pt.com.broker.types.NetSubscribe;

public class StatisticsCollector
{
	private static Logger log = LoggerFactory.getLogger(StatisticsCollector.class);

	private final String SUBSCRIPTION = "/system/stats/.*";
	private final BrokerClient brokerClient;

	private static final XMLInputFactory factory = XMLInputFactory.newInstance();

	public StatisticsCollector(BrokerClient brokerClient)
	{
		this.brokerClient = brokerClient;

	}

	public void start()
	{
		BrokerClient bc = getBrokerClient();

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

		if (log.isDebugEnabled())
		{
			String xml = new String(notification.getMessage().getPayload());
			log.debug("Message received: '{}'", xml);
		}

		try
		{

			XMLStreamReader staxXmlReader = factory.createXMLStreamReader(new InputStreamReader(new ByteArrayInputStream(notification.getMessage().getPayload())));

			String agent = null;
			Date sampleStartDate = null;

			int eventType = staxXmlReader.getEventType();
			do
			{
				if (eventType == XMLStreamConstants.START_ELEMENT)
				{
					String lname = staxXmlReader.getLocalName();

					if (lname.equals("qinfo") || lname.equals("mqinfo"))
					{
						agent = staxXmlReader.getAttributeValue("", "agent-name");
						String sampleStart = staxXmlReader.getAttributeValue("", "date");
						sampleStartDate = DateUtil.parseISODate(sampleStart);

						// System.out.printf("agent: %s; date: %s%n", agent, sampleStartDate);
					}
					else if (lname.equals("item"))
					{
						String subject = staxXmlReader.getAttributeValue("", "subject");
						String predicate = staxXmlReader.getAttributeValue("", "predicate");
						String svalue = staxXmlReader.getAttributeValue("", "value");
						double value = Double.parseDouble(svalue);

						if ((agent != null) && (sampleStartDate != null))
						{
							// System.out.printf("\t -> subject: %s; predicate: %s; value: %s%n", subject, predicate, value);
							processItem(agent, sampleStartDate, subject, predicate, value);
						}
					}
				}
				eventType = staxXmlReader.next();
			}
			while (eventType != XMLStreamConstants.END_DOCUMENT);

		}
		catch (Throwable t)
		{
			log.error(String.format("Failed to process received message. Error: %s. Message: \n'%s'", t.getMessage(), new String(notification.getMessage().getPayload())));
		}
	}

	private void processItem(String agent, Date sampleStartDate, String subject, String predicate, double value)
	{
		StatisticsDB.add(agent, sampleStartDate, subject, predicate, value);
	}

	public BrokerClient getBrokerClient()
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
