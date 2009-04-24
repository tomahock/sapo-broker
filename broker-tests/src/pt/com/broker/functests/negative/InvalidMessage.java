package pt.com.broker.functests.negative;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.mina.core.buffer.IoBuffer;

import pt.com.broker.functests.helpers.GenericNegativeTest;
import pt.com.protobuf.codec.ProtoBufBindingSerializer;
import pt.com.protobuf.codec.ProtoBufEncoder;
import pt.com.thrift.codec.ThriftBindingSerializer;
import pt.com.types.BindingSerializer;
import pt.com.types.NetAction;
import pt.com.types.NetMessage;
import pt.com.types.NetPing;
import pt.com.types.NetAction.ActionType;
import pt.com.xml.codec.SoapBindingSerializer;
import pt.com.xml.codec.SoapEncoderV2;

public class InvalidMessage extends GenericNegativeTest
{
	public InvalidMessage()
	{
		super("Invalid Message");
		setDataToSend(buildMessage());
	}

	private byte[] buildMessage()
	{
		NetAction action = new NetAction(ActionType.PING);
		NetPing ping = new NetPing(System.currentTimeMillis());
		action.setPingMessage(ping);
		NetMessage message = new NetMessage(action);

		BindingSerializer encoder = null;

		switch (getEncodingProtocolType())
		{
		case SOAP:
			encoder = new SoapBindingSerializer();
			break;
		case PROTOCOL_BUFFER:
			encoder = new ProtoBufBindingSerializer();
			break;
		case THRIFT:
			encoder = new ThriftBindingSerializer();
			break;
		}

		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
		
		try
		{
			byte[] headerWithoutSize = new byte[] { 0, (byte) getEncodingProtocolType().ordinal(), 0, 0 };

			byte[] rawData = encoder.marshal(message);
			
			bitInvert(rawData, 0, rawData.length);

			dataOutputStream.write(headerWithoutSize);
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
}
