package pt.com.broker.functests.negative;

import pt.com.broker.functests.helpers.GenericNegativeTest;
import pt.com.broker.types.NetProtocolType;

public class BadEncodingVersionTest extends GenericNegativeTest
{

    public BadEncodingVersionTest(NetProtocolType protocolType) {
        super(protocolType);

		setName("Bad Encoding Version Test");

		setDataToSend(new byte[] { 0, 1, 0, (byte) 0xff, (byte) 0, (byte) 0, (byte) 0, (byte) 2, 0, 0 });

		setFaultCode("1103");
		setFaultMessage("Unknown encoding version");

	}

	public void addConsequece()
	{
	}

	/*@Override
	public boolean skipTest()
	{
		return true;
	}*/
}