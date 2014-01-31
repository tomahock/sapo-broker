package pt.com.broker.functests.positive;

import org.caudexorigo.text.RandomStringUtils;

import pt.com.broker.client.BrokerClient;
import pt.com.broker.functests.Action;
import pt.com.broker.functests.Consequence;
import pt.com.broker.functests.Step;
import pt.com.broker.functests.conf.ConfigurationInfo;
import pt.com.broker.functests.helpers.BrokerTest;
import pt.com.broker.types.NetNotification;
import pt.com.broker.types.NetProtocolType;

public class PollNoWaitTest extends BrokerTest
{
	private String baseName = RandomStringUtils.randomAlphanumeric(10);
	private String queueName = String.format("/poll/%s", baseName);

	public PollNoWaitTest()
	{
		super("Poll No Wait test");
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
					BrokerClient bk = new BrokerClient(ConfigurationInfo.getParameter("agent1-host"), BrokerTest.getAgent1Port(), "tcp://mycompany.com/test", getEncodingProtocolType());

					NetNotification msg = bk.poll(queueName, -1, null);

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

	@Override
	public boolean skipTest()
	{
		return (getEncodingProtocolType() == NetProtocolType.SOAP) || (getEncodingProtocolType() == NetProtocolType.SOAP_v0) || (getEncodingProtocolType() == NetProtocolType.JSON);
	}
}
