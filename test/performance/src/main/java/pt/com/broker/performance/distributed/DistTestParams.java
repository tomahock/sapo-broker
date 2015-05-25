package pt.com.broker.performance.distributed;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import org.caudexorigo.io.UnsynchronizedByteArrayInputStream;
import org.caudexorigo.io.UnsynchronizedByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.types.NetAction.DestinationType;
import pt.com.broker.types.NetProtocolType;

public class DistTestParams
{
	private static final Logger log = LoggerFactory.getLogger(DistTestParams.class);

	public static class ClientInfo
	{
		private String name;
		private String agentHost;
		private int port;

		private ClientInfo()
		{
		}

		public ClientInfo(String name, String agentHost, int port)
		{
			this.name = name;
			this.agentHost = agentHost;
			this.port = port;
		}

		public String getName()
		{
			return name;
		}

		public String getAgentHost()
		{
			return agentHost;
		}

		public int getPort()
		{
			return port;
		}

		public void write(ObjectOutputStream outputObj)
		{
			try
			{
				outputObj.writeUTF(name);
				outputObj.writeUTF(agentHost);
				outputObj.writeInt(port);
			}
			catch (IOException e)
			{
				log.error("Failed to serialize object", e);
			}
		}

		public void read(ObjectInputStream inputObj)
		{
			try
			{
				this.name = inputObj.readUTF();
				this.agentHost = inputObj.readUTF();
				this.port = inputObj.readInt();
			}
			catch (Throwable e)
			{
				log.error("Failed to deserialize object", e);
			}
		}
	}

	private String testName;

	private String destination;
	private DestinationType destinationType;
	private int messageSize;
	private int numberOfMessagesToSend;
	private boolean syncConsumer;
	private boolean isNoAckConsumer;
	private NetProtocolType encoding;

	private ClientInfo clientInfo;

	private final HashMap<String, ClientInfo> producers = new HashMap<String, ClientInfo>(); // name of each producer
	private final HashMap<String, ClientInfo> consumers = new HashMap<String, ClientInfo>(); // name of each consumer

	private DistTestParams()
	{

	}

	public DistTestParams(String testName, String destination, DestinationType destinationType, int messageSize, int numberOfMessagesToSend, boolean syncConsumer, boolean isNoAckConsumer, NetProtocolType encoding)
	{
		this.testName = testName;
		this.destination = destination;
		this.destinationType = destinationType;
		this.messageSize = messageSize;
		this.numberOfMessagesToSend = numberOfMessagesToSend;
		this.syncConsumer = syncConsumer;
		this.isNoAckConsumer = isNoAckConsumer;
		this.encoding = encoding;
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

	public int getNumberOfMessagesToSend()
	{
		return numberOfMessagesToSend;
	}

	public HashMap<String, ClientInfo> getProducers()
	{
		return producers;
	}

	public HashMap<String, ClientInfo> getConsumers()
	{
		return consumers;
	}

	public boolean isSyncConsumer()
	{
		return syncConsumer;
	}

	public boolean isNoAckConsumer()
	{
		return isNoAckConsumer;
	}

	public NetProtocolType getEncoding()
	{
		return encoding;
	}

	public byte[] serialize(ClientInfo clientInfo)
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
			outputObj.writeInt(numberOfMessagesToSend);
			outputObj.writeBoolean(syncConsumer);
			outputObj.writeBoolean(isNoAckConsumer);
			outputObj.writeUTF(encoding.toString());

			clientInfo.write(outputObj);

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
			distTestParams.destinationType = DestinationType.valueOf(inputObj.readUTF());
			distTestParams.messageSize = inputObj.readInt();
			distTestParams.numberOfMessagesToSend = inputObj.readInt();
			distTestParams.syncConsumer = inputObj.readBoolean();
			distTestParams.isNoAckConsumer = inputObj.readBoolean();
			distTestParams.encoding = NetProtocolType.valueOf(inputObj.readUTF());

			distTestParams.clientInfo = new ClientInfo();
			distTestParams.clientInfo.read(inputObj);

		}
		catch (Throwable e)
		{
			log.error("Failed to deserialize object", e);
			return null;
		}

		return distTestParams;
	}

	public ClientInfo getClientInfo()
	{
		return clientInfo;
	}

}
