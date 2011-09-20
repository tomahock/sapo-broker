package pt.com.broker.functests.negative;

import java.util.Arrays;
import java.util.Random;

import pt.com.broker.functests.Prerequisite;
import pt.com.broker.functests.Step;
import pt.com.broker.functests.helpers.GenericNegativeTest;
import pt.com.broker.types.NetProtocolType;

public class InvalidRandomMessageTest extends GenericNegativeTest
{
	public InvalidRandomMessageTest()
	{
		super("Invalid Message - Random content with valid enconding and length");

		int msgSize = 200;
		byte[] randomData = new byte[msgSize];
		new Random().nextBytes(randomData);

		setTimeout(10 * 1000);
		byte[] header = null;
		if (getEncodingProtocolType() != NetProtocolType.SOAP_v0)
		{
			header = new byte[] { 0, (byte) getEncodingProtocolType().ordinal(), 0, 0, (byte) 0, (byte) 0, (byte) 0, (byte) msgSize };
		}
		else
		{
			header = new byte[] { (byte) 0, (byte) 0, (byte) 0, (byte) msgSize };
		}

		byte[] binMsg = Arrays.copyOf(header, header.length + msgSize);
		int idx = header.length;
		for (byte b : randomData)
		{
			binMsg[idx++] = b;
		}
		setDataToSend(binMsg);

		setFaultCode("1201");
		setFaultMessage("Invalid message format");

		// setOkToTimeOut(true);
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

	@Override
	public boolean skipTest()
	{
		return (getEncodingProtocolType() == NetProtocolType.JSON);
	}
}
