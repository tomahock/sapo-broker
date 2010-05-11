package pt.com.broker.monitorization.collector;

import java.io.StringReader;
import java.util.Date;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import org.caudexorigo.text.DateUtil;

public class StaxTest
{

	private static final XMLInputFactory factory = XMLInputFactory.newInstance();

	public static void main(String[] args) throws Throwable
	{
		String xml = "<qinfo date='2010-05-04T10:11:56.570Z' agent-name='10.135.65.11:3315'><item subject='queue://AsyncServices@/pond/asyncservices' predicate='subscriptions' value='5.9' /><item subject='queue://alertas@/sapo/alerts/delivery/http' predicate='subscriptions' value='0' /></qinfo>";

		XMLStreamReader staxXmlReader = factory.createXMLStreamReader(new StringReader(xml));

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

					System.out.printf("agent: %s; date: %s%n", agent, sampleStartDate);
				}
				else if (lname.equals("item"))
				{
					String subject = staxXmlReader.getAttributeValue("", "subject");
					String predicate = staxXmlReader.getAttributeValue("", "predicate");
					String svalue = staxXmlReader.getAttributeValue("", "value");
					double value = Double.parseDouble(svalue);

					if ((agent != null) && (sampleStartDate != null))
					{
						System.out.printf("\t -> subject: %s; predicate: %s; value: %s%n", subject, predicate, value);
						// processItem(agent, sampleStartDate, subject, predicate, value);
					}
				}
			}
			eventType = staxXmlReader.next();
		}
		while (eventType != XMLStreamConstants.END_DOCUMENT);

	}

}
