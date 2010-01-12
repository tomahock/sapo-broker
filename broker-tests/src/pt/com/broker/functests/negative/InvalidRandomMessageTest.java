package pt.com.broker.functests.negative;

import java.util.Arrays;
import java.util.Random;

import pt.com.broker.functests.helpers.GenericNegativeTest;

public class InvalidRandomMessageTest extends GenericNegativeTest
{
	public InvalidRandomMessageTest()
	{
		super("Invalid Message - Random content with valid enconding and length");

		int msgSize = 200;
		byte[] randomData = new byte[msgSize];
		new Random().nextBytes(randomData);

		byte[] header = new byte[] { 0, (byte) getEncodingProtocolType().ordinal(), 0, 0, (byte) 0, (byte) 0, (byte) 0, (byte) msgSize };

		byte[] binMsg = Arrays.copyOf(header, header.length + msgSize);
		int idx = header.length;
		for (byte b : randomData)
		{
			binMsg[idx++] = b;
		}
		setDataToSend(binMsg);

		setFaultCode("1201");
		setFaultMessage("Invalid message format");
		
		setOkToTimeOut(true);
	}
}
