package pt.com.broker.functests.negative;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import pt.com.broker.codec.protobuf.JsonCodecForProtoBuf;
import pt.com.broker.codec.protobuf.ProtoBufBindingSerializer;
import pt.com.broker.codec.thrift.ThriftBindingSerializer;
import pt.com.broker.codec.xml.SoapBindingSerializer;
import pt.com.broker.functests.Prerequisite;
import pt.com.broker.functests.Step;
import pt.com.broker.functests.helpers.GenericNegativeTest;
import pt.com.broker.types.BindingSerializer;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetAction.ActionType;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetPing;
import pt.com.broker.types.NetProtocolType;

public class InvalidMessageTest extends GenericNegativeTest
{
	public InvalidMessageTest()
	{
		super("Invalid Message - Ping with bits inverted");
		setDataToSend(buildMessage());

		setFaultCode("1201");
		setFaultMessage("Invalid message format");

		getPrerequisites().add(new Prerequisite("Ping")
		{

			@Override
			public Step run() throws Exception
			{
				try
				{
					getBrokerClient().checkStatus();
					setSucess(true);
					setDone(true);
				}
				catch (Throwable e)
				{
					setReasonForFailure(e.getMessage());
				}
				return this;
			}
		});
	}

	private byte[] buildMessage()
	{
		NetAction action = new NetAction(ActionType.PING);
		NetPing ping = new NetPing("ACTIONID");
		action.setPingMessage(ping);
		NetMessage message = new NetMessage(action);

		BindingSerializer encoder = null;

		switch (getEncodingProtocolType())
		{
		case SOAP_v0:
		case SOAP:
			encoder = new SoapBindingSerializer();
			break;
		case PROTOCOL_BUFFER:
			encoder = new ProtoBufBindingSerializer();
			break;
		case THRIFT:
			encoder = new ThriftBindingSerializer();
			break;
		case JSON:
			encoder = new JsonCodecForProtoBuf();
		}

		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);

		try
		{
			byte[] headerWithoutSize = new byte[] { 0, (byte) getEncodingProtocolType().ordinal(), 0, 0 };

			byte[] rawData = encoder.marshal(message);

			bitInvert(rawData, 0, rawData.length);

			if (!getEncodingProtocolType().equals(NetProtocolType.SOAP_v0))
			{
				dataOutputStream.write(headerWithoutSize);
			}
			dataOutputStream.writeInt(rawData.length);
			dataOutputStream.write(rawData);
		}
		catch (IOException e)
		{
			setFailure(e);
		}

		byte[] byteArray = byteArrayOutputStream.toByteArray();

		return byteArray;
	}

	private void bitInvert(byte[] array, int start, int end)
	{
		if ((start < 0) || (end < 1) || (end <= start))
			return;
		if ((start >= array.length) || (end > array.length))
			return;

		do
		{
			array[start++] ^= (byte) 0xff;
		}
		while (start != end);
	}

	@Override
	public boolean skipTest()
	{
		return (getEncodingProtocolType() == NetProtocolType.JSON);
	}
}
