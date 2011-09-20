package pt.com.broker.functests.negative;

import java.util.concurrent.TimeoutException;

import org.caudexorigo.text.RandomStringUtils;

import pt.com.broker.client.BrokerClient;
import pt.com.broker.functests.Action;
import pt.com.broker.functests.Consequence;
import pt.com.broker.functests.Step;
import pt.com.broker.functests.conf.ConfigurationInfo;
import pt.com.broker.functests.helpers.BrokerTest;
import pt.com.broker.functests.helpers.GenericNetMessageNegativeTest;
import pt.com.broker.types.NetProtocolType;

public class TimeoutPollTest extends GenericNetMessageNegativeTest
{
	private String baseName = RandomStringUtils.randomAlphanumeric(10);
	private String queueName = String.format("/poll/%s", baseName);

	public TimeoutPollTest()
	{
		super("Time out poll test");

		setFaultCode("2005");
		setFaultMessage("Message poll timeout");
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
				boolean success = false;
				try
				{
					BrokerClient bk = new BrokerClient(ConfigurationInfo.getParameter("agent1-host"), BrokerTest.getAgent1Port(), "tcp://mycompany.com/test", getEncodingProtocolType());

					bk.poll(queueName, 500, null);

					bk.close();

				}
				catch (TimeoutException t)
				{
					success = true;
				}
				catch (Throwable t)
				{
				}
				setDone(true);
				setSucess(success);
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
