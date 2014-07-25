package pt.com.broker.client.sample;

import org.caudexorigo.cli.CliFactory;
import org.caudexorigo.concurrent.Sleep;
import org.caudexorigo.text.RandomStringUtils;
import pt.com.broker.client.CliArgs;
import pt.com.broker.client.UdpClient;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.broker.types.NetBrokerMessage;
import pt.com.broker.types.NetPublish;

/**
 * Simple UDP producer sample.
 * 
 */
public class UDPProducer
{
	private String host;
	private int udpPort;
	private DestinationType dtype;
	private String dname;

	public static void main(String[] args) throws Throwable
	{
		final CliArgs cargs = CliFactory.parseArguments(CliArgs.class, args);
		UDPProducer producer = new UDPProducer();

		producer.host = cargs.getHost();
		producer.udpPort = cargs.getUdpPort();
		producer.dtype = DestinationType.valueOf(cargs.getDestinationType());
		producer.dname = cargs.getDestination();

		UdpClient bk = new UdpClient(producer.host, producer.udpPort);

		// System.out.printf("Start sending a string of %s random alphanumeric characters in 2 seconds...%n", cargs.getMessageLength());
		//
		Thread.sleep(500);

		producer.sendLoop(bk, cargs.getMessageLength());
	}

	private void sendLoop(UdpClient bk, int messageLength) throws Throwable
	{

		for (int i = 0; i < 1000000; i++)
		{
			final String msg = RandomStringUtils.randomAlphanumeric(messageLength);
			NetBrokerMessage brokerMessage = new NetBrokerMessage(msg.getBytes("UTF-8"));
			NetPublish publishMsg = new NetPublish(dname, dtype, brokerMessage);
			bk.publish(publishMsg);

			System.out.printf("%s -> Destination: '%s#%s';  Message: '%s'%n", i, dtype, dname, msg);

			Sleep.time(500);
		}
	}
}