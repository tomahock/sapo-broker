package pt.com.broker.functests.negative;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import pt.com.broker.functests.helpers.GenericNegativeTest;
import pt.com.protobuf.codec.ProtoBufBindingSerializer;
import pt.com.thrift.codec.ThriftBindingSerializer;
import pt.com.types.BindingSerializer;
import pt.com.types.NetAction;
import pt.com.types.NetMessage;
import pt.com.types.NetPing;
import pt.com.types.NetAction.ActionType;
import pt.com.xml.codec.SoapBindingSerializer;

public class InvalidRandomMessageTest extends GenericNegativeTest
{
	public InvalidRandomMessageTest()
	{
		super("Invalid Message - Random content with valid enconding and length");
		
		int msgSize = 200;
		byte[] randomData = new byte[msgSize];
		new Random().nextBytes(randomData);
		
		byte[] header = new byte[] { 0, (byte) getEncodingProtocolType().ordinal(), 0, 0, (byte) 0, (byte) 0, (byte) 0, (byte) msgSize};
		
		byte[] binMsg = Arrays.copyOf(header, header.length + msgSize);
		int idx = header.length;
		for(byte b : randomData)
		{
			binMsg[idx++] = b; 
		}
		setDataToSend(binMsg);
		
		setFaultCode("1201");
		setFaultMessage("Invalid message format");
	}
}
