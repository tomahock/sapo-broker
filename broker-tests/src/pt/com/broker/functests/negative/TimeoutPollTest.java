package pt.com.broker.functests.negative;

import java.util.concurrent.TimeoutException;

import org.caudexorigo.text.RandomStringUtils;

import pt.com.broker.client.BrokerClient;
import pt.com.broker.functests.Action;
import pt.com.broker.functests.Consequence;
import pt.com.broker.functests.Step;
import pt.com.broker.functests.conf.ConfigurationInfo;
import pt.com.broker.functests.helpers.GenericNetMessageNegativeTest;
import pt.com.broker.types.NetNotification;
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
		
		//setOkToTimeOut(true);
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
					BrokerClient bk = new BrokerClient(ConfigurationInfo.getParameter("agent1-host"), 
							Integer.parseInt(ConfigurationInfo.getParameter("agent1-port")), "tcp://mycompany.com/test", getEncodingProtocolType());

					bk.poll(queueName, 500, null);

					System.out.println("TimeoutPollTest.build().new Consequence() {...}.run() - poll returned");
					bk.close();

				}
				catch (TimeoutException t)
				{
					System.out.println("TimeoutPollTest.build().new Consequence() {...}.run() - TimeoutException");
					success = true;
				}
				catch (Throwable t)
				{
					System.out.println("TimeoutPollTest.build().new Consequence() {...}.run() - Throwable");
					t.printStackTrace();
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
		return getEncodingProtocolType() == NetProtocolType.SOAP;
	}
}
