package pt.com.broker.functests.negative;

import java.util.Random;

import pt.com.broker.functests.helpers.GenericNegativeTest;
import pt.com.broker.types.NetProtocolType;

public class TotallyInvalidRandomMessageTest extends GenericNegativeTest
{

	public TotallyInvalidRandomMessageTest(NetProtocolType protocolType)
	{
		super(protocolType);

		setName("Invalid Message - Random content");

		int msgSize = 200;
		byte[] randomData = new byte[msgSize];
		new Random().nextBytes(randomData);

		setDataToSend(randomData);
		setOkToTimeOut(true);
	}

	@Override
	public boolean skipTest()
	{
		return (getEncodingProtocolType() == NetProtocolType.JSON);
	}
}
