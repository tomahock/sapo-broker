package pt.com.broker.functests.negative;

import pt.com.broker.functests.helpers.GenericNegativeTest;

public class MessageSizeBiggerThanMessageTest extends GenericNegativeTest
{
	public MessageSizeBiggerThanMessageTest()
	{
		super("MessageSizeBiggerThanMessageTest");

		setDataToSend(new byte[] { 0, (byte) getDefaultEncodingProtocolType().ordinal(), 0, 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0xff, 0, 0 });

		setOkToTimeOut(true);
	}
}
