package pt.com.broker.functests.negative;

import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized;

import pt.com.broker.functests.helpers.GenericNegativeTest;
import pt.com.broker.types.NetProtocolType;

public class BadEncodingTypeTest extends GenericNegativeTest
{

	public BadEncodingTypeTest(NetProtocolType protocolType)
	{
		super(protocolType);

		setName("Bad Encoding Type Test");

		setDataToSend(new byte[] { 0, (byte) 0xff, 0, 0, (byte) 0, (byte) 0, (byte) 0, (byte) 2, 0, 0 });

		setFaultCode("1102");
		setFaultMessage("Unknown encoding protocol");

	}

	public void addConsequece()
	{
	}

	/*
	 * @Override public boolean skipTest() { return true; }
	 */

	@Parameterized.Parameters()
	public static Collection getProtocolTypes()
	{
		return Arrays.asList(new Object[][] {
				{ NetProtocolType.PROTOCOL_BUFFER },
				{ NetProtocolType.THRIFT },
				{ NetProtocolType.JSON },
				{ NetProtocolType.SOAP },
		});
	}
}
