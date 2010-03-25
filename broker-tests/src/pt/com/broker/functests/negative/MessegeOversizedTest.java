package pt.com.broker.functests.negative;

import java.util.List;

import pt.com.broker.functests.Epilogue;
import pt.com.broker.functests.Prerequisite;
import pt.com.broker.functests.Step;
import pt.com.broker.functests.helpers.GenericNegativeTest;

public class MessegeOversizedTest extends GenericNegativeTest
{

	public MessegeOversizedTest()
	{
		super("Message oversize");

		setDataToSend(new byte[] { 0, (byte) getEncodingProtocolType().ordinal(), 0, 0, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, 0, 0 });

		setFaultCode("1101");
		setFaultMessage("Invalid message size");
		
		getPrerequisites().add( new Prerequisite("Ping")
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
}
