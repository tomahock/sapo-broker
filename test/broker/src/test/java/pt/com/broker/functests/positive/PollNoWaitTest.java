package pt.com.broker.functests.positive;

import java.util.Arrays;
import java.util.Collection;

import org.caudexorigo.text.RandomStringUtils;
import org.junit.runners.Parameterized;

import pt.com.broker.client.nio.BrokerClient;
import pt.com.broker.functests.Action;
import pt.com.broker.functests.Consequence;
import pt.com.broker.functests.Step;
import pt.com.broker.functests.helpers.BrokerTest;
import pt.com.broker.types.NetNotification;
import pt.com.broker.types.NetProtocolType;

public class PollNoWaitTest extends BrokerTest
{
	private String baseName = RandomStringUtils.randomAlphanumeric(10);
	private String queueName = String.format("/poll/%s", baseName);

	public PollNoWaitTest(NetProtocolType protocolType)
	{
		super(protocolType);

		setName("Poll No Wait test");
	}

	@Override
	protected void build() throws Throwable
	{
		setAction(new Action("Poll Test", "Producer")
		{

			@Override
			public Step run() throws Exception
			{
				setDone(true);
				setSucess(true);
				return this;
			}

		});

		addConsequences(new Consequence("Poll Test", "Consumer")
		{
			@Override
			public Step run() throws Exception
			{
				try
				{
					BrokerClient bk = new BrokerClient(getAgent1Hostname(), getAgent1Port(), getEncodingProtocolType());
					bk.connect();

					NetNotification msg = bk.poll(queueName, -1);

					if (msg == null)
					{
						setDone(true);
						setSucess(true);
					}
					else
					{
						setReasonForFailure("Unexpectected message received...");
						return this;
					}

					bk.close();
				}
				catch (Throwable t)
				{
					throw new Exception(t);
				}
				return this;
			}

		});
	}

	/*
	 * @Override public boolean skipTest() { return (getEncodingProtocolType() == NetProtocolType.SOAP) || (getEncodingProtocolType() == NetProtocolType.SOAP_v0) || (getEncodingProtocolType() == NetProtocolType.JSON); }
	 */
	@Parameterized.Parameters()
	public static Collection getProtocolTypes()
	{
		return Arrays.asList(new Object[][] {
				{ NetProtocolType.THRIFT },
				{ NetProtocolType.PROTOCOL_BUFFER },
		});
	}
}
