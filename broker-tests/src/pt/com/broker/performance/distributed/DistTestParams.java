package pt.com.broker.performance.distributed;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.caudexorigo.io.UnsynchronizedByteArrayInputStream;
import org.caudexorigo.io.UnsynchronizedByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.types.NetAction.DestinationType;

public class DistTestParams
{
	private static final Logger log = LoggerFactory.getLogger(DistTestParams.class);
	
	
	private String testName;
	
	private String destination;
	private DestinationType destinationType;
	private int messageSize;
	private int numberOfMessagesToReceive;
	private int numberOfMessagesToSend;
	
	private final List<String> producers = new ArrayList<String>(); // name of each producer
	private final List<String> consumers = new ArrayList<String>(); // name of each consumer
	
	private DistTestParams()
	{
		
	}
	
	public DistTestParams(String testName, String destination, DestinationType destinationType, int messageSize, int numberOfMessagesToReceive, int numberOfMessagesToSend)
	{
		this.testName = testName;
		this.destination = destination;
		this.destinationType = destinationType;
		this.messageSize = messageSize;
		this.numberOfMessagesToReceive = numberOfMessagesToReceive;
		this.numberOfMessagesToSend = numberOfMessagesToSend;
		
	}


	public String getTestName()
	{
		return testName;
	}

	public String getDestination()
	{
		return destination;
	}

	public DestinationType getDestinationType()
	{
		return destinationType;
	}

	public int getMessageSize()
	{
		return messageSize;
	}

	public int getNumberOfMessagesToReceive()
	{
		return numberOfMessagesToReceive;
	}
	
	public int getNumberOfMessagesToSend()
	{
		return numberOfMessagesToSend;
	}

	public List<String> getProducers()
	{
		return producers;
	}


	public List<String> getConsumers()
	{
		return consumers;
	}
	
	public byte[] serialize()
	{
		byte[] data = null;
		try
		{
			UnsynchronizedByteArrayOutputStream bout = new UnsynchronizedByteArrayOutputStream();
			ObjectOutputStream outputObj = new ObjectOutputStream(bout);
			outputObj.writeUTF(testName);
			outputObj.writeUTF(destination);
			outputObj.writeUTF(destinationType.toString());
			outputObj.writeInt(messageSize);
			outputObj.writeInt(numberOfMessagesToReceive);
			outputObj.writeInt(numberOfMessagesToSend);			
			outputObj.flush();

			data = bout.toByteArray();			
		}
		catch (IOException e)
		{
			log.error("Failed to serialize object", e);
		}
		return data;
	}
	
	public static DistTestParams deserialize(byte[] data)
	{
		DistTestParams distTestParams = new DistTestParams();
		
		try
		{
			ObjectInputStream inputObj;
			inputObj = new ObjectInputStream(new UnsynchronizedByteArrayInputStream(data));
			
			distTestParams.testName = inputObj.readUTF();
			distTestParams.destination = inputObj.readUTF();
			distTestParams.destinationType = DestinationType.valueOf( inputObj.readUTF() );
			distTestParams.messageSize = inputObj.readInt();
			distTestParams.numberOfMessagesToReceive = inputObj.readInt();
			distTestParams.numberOfMessagesToSend = inputObj.readInt();
			
		}
		catch (Throwable e)
		{
			log.error("Failed to deserialize object", e);
			return null;
		}
		
		return distTestParams;
	}
	
}
