package pt.com.broker.functests.negative;

import pt.com.broker.functests.Prerequisite;
import pt.com.broker.functests.Step;
import pt.com.broker.functests.helpers.GenericNegativeTest;
import pt.com.broker.types.NetProtocolType;

public class MessegeOversizedTest extends GenericNegativeTest
{

	public MessegeOversizedTest()
	{
		super("Message oversize");

		if (getEncodingProtocolType() != NetProtocolType.SOAP_v0)
		{
			setDataToSend(new byte[] { 0, (byte) getEncodingProtocolType().ordinal(), 0, 0, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, 0, 0 });
		}
		else
		{
			setDataToSend(new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, 0, 0 });
		}

		setFaultCode("1101");
		setFaultMessage("Invalid message size");

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
		return (getEncodingProtocolType() == NetProtocolType.SOAP) || (getEncodingProtocolType() == NetProtocolType.SOAP_v0) || (getEncodingProtocolType() == NetProtocolType.JSON);
	}
}
