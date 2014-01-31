package pt.com.broker.client.sample;

import java.util.UUID;

import org.caudexorigo.Shutdown;
import org.caudexorigo.text.RandomStringUtils;

import pt.com.broker.client.BrokerClient;
import pt.com.broker.types.NetBrokerMessage;

public class HighQueueNumber
{

	public static void main(String[] args)
	{
		try
		{
			BrokerClient bk = new BrokerClient("localhost", 3323);
			int msg_count = 5000;

			for (int i = 0; i < msg_count; ++i)
			{
				final String msg = i + " - " + RandomStringUtils.randomAlphanumeric(150);
				final String dname = String.format("/pay/trans-id/%s", UUID.randomUUID().toString());

				NetBrokerMessage bk_msg = new NetBrokerMessage(msg);

				bk.enqueueMessage(bk_msg, dname);
				System.out.printf("%s: %s -> Send Message: %s%n", i, dname, msg);
			}

			bk.close();
		}
		catch (Throwable t)
		{
			Shutdown.now(t);

		}
	}
}