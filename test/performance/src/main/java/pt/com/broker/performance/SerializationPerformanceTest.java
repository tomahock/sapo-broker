package pt.com.broker.performance;

import org.caudexorigo.text.RandomStringUtils;

import pt.com.broker.codec.protobuf.ProtoBufBindingSerializer;
import pt.com.broker.codec.thrift.ThriftBindingSerializer;
import pt.com.broker.codec.xml.SoapBindingSerializer;
import pt.com.broker.types.BindingSerializer;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetAction.ActionType;
import pt.com.broker.types.NetBrokerMessage;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetPublish;

public class SerializationPerformanceTest
{
	private static int LOOPS = 10000;

	private static int MESSAGE_SIZE = 1024;

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{

		String randomContent = RandomStringUtils.random(MESSAGE_SIZE);

		NetAction action = new NetAction(ActionType.PUBLISH);
		NetPublish publish = new NetPublish("destination", pt.com.broker.types.NetAction.DestinationType.QUEUE, new NetBrokerMessage(randomContent));
		action.setPublishMessage(publish);
		NetMessage message = new NetMessage(action);

		BindingSerializer soapEncoder = new SoapBindingSerializer();
		BindingSerializer protoBufEncoder = new ProtoBufBindingSerializer();
		BindingSerializer thriftEncoder = new ThriftBindingSerializer();

		byte[] soapRawData = performEncodeTest(soapEncoder, message);
		byte[] protoBufRawData = performEncodeTest(protoBufEncoder, message);
		byte[] thriftRawData = performEncodeTest(thriftEncoder, message);

		System.out.println("");

		performDecodeTest(soapEncoder, soapRawData);
		performDecodeTest(protoBufEncoder, protoBufRawData);
		performDecodeTest(thriftEncoder, thriftRawData);
	}

	private static byte[] performEncodeTest(BindingSerializer serializer, NetMessage message)
	{
		long startTime = System.nanoTime();
		byte[] rawData = null;
		try
		{
			for (int i = 0; i != LOOPS; ++i)
			{
				rawData = serializer.marshal(message);
			}
		}
		catch (Throwable t)
		{
			System.err.println("Failed to serialize: " + serializer + " " + t.getMessage());
			t.printStackTrace();
			return null;
		}
		long stopTime = System.nanoTime();

		System.out.printf("Serialization time: '%.2f' using %s\n", (double) ((stopTime - startTime) / (1000 * 1000)), serializer.getClass().getCanonicalName());
		return rawData;
	}

	private static void performDecodeTest(BindingSerializer serializer, byte[] data)
	{
		if (data == null)
			return;

		long startTime = System.nanoTime();
		try
		{
			for (int i = 0; i != LOOPS; ++i)
			{
				NetMessage unmarshal = serializer.unmarshal(data);
			}
		}
		catch (Throwable t)
		{
			System.err.println("Failed to serialize: " + serializer);
		}
		long stopTime = System.nanoTime();

		System.out.printf("Deserialization time: '%.2f' using %s\n", (double) ((stopTime - startTime) / (1000 * 1000)), serializer.getClass().getCanonicalName());

	}

}
