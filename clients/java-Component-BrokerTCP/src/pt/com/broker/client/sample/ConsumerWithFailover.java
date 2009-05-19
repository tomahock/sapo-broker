package pt.com.broker.client.sample;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import org.caudexorigo.cli.CliFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.client.BrokerClient;
import pt.com.broker.client.CliArgs;
import pt.com.broker.client.HostInfo;
import pt.com.broker.client.messaging.BrokerListener;
import pt.com.broker.types.NetNotification;
import pt.com.broker.types.NetProtocolType;
import pt.com.broker.types.NetSubscribe;
import pt.com.broker.types.NetAction.DestinationType;

public class ConsumerWithFailover implements BrokerListener
{
	private static final Logger log = LoggerFactory.getLogger(Consumer.class);
	private final AtomicInteger counter = new AtomicInteger(0);

	private String host;
	private int port;
	private DestinationType dtype;
	private String dname;

	public static void main(String[] args) throws Throwable
	{
		final CliArgs cargs = CliFactory.parseArguments(CliArgs.class, args);

		ConsumerWithFailover consumer = new ConsumerWithFailover();

		consumer.dtype = DestinationType.valueOf(cargs.getDestinationType());
		consumer.dname = cargs.getDestination();

		Collection<HostInfo> hosts = new ArrayList<HostInfo>(2);
		hosts.add(new HostInfo("localhost", 3423));
		hosts.add(new HostInfo("localhost", 3323));

		BrokerClient bk = new BrokerClient(hosts, "tcp://mycompany.com/mysniffer", NetProtocolType.THRIFT);

		NetSubscribe subscribe = new NetSubscribe(consumer.dname, consumer.dtype);

		bk.addAsyncConsumer(subscribe, consumer);

		System.out.println("listening...");
	}

	@Override
	public boolean isAutoAck()
	{
		if (dtype == DestinationType.TOPIC)
		{
			return false;
		}
		else
		{
			return true;
		}
	}

	@Override
	public void onMessage(NetNotification notification)
	{
		log.info(String.format("%s -> Received Message Length: %s (%s)", counter.incrementAndGet(), notification.getMessage().getPayload().length, new String(notification.getMessage().getPayload())));
	}
}
