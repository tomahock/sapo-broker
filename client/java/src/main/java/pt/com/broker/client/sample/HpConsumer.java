package pt.com.broker.client.sample;

import pt.com.broker.client.BrokerClient;
import pt.com.broker.client.messaging.BrokerListener;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.broker.types.NetNotification;
import pt.com.broker.types.NetSubscribe;

public class HpConsumer implements BrokerListener
{

	private String host;
	private int port;
	private DestinationType dtype;
	private String dname;
	private BrokerClient bk;

	public static void main(String[] args) throws Throwable
	{

		HpConsumer consumer = new HpConsumer();

		consumer.host = "10.135.5.139";
		consumer.port = 3323;
		consumer.dtype = DestinationType.VIRTUAL_QUEUE;
		consumer.dname = "GOLIAS_KPI_GLOBAL_AGG@/sapo/event-agg-kpi/hp.sapo.pt";

		consumer.bk = new BrokerClient(consumer.host, consumer.port);

		NetSubscribe subscribe = new NetSubscribe(consumer.dname, consumer.dtype);

		consumer.bk.addAsyncConsumer(subscribe, consumer);

	}

	@Override
	public boolean isAutoAck()
	{
		return false;
	}

	@Override
	public void onMessage(NetNotification notification)
	{
		String payload = new String(notification.getMessage().getPayload());

		if (payload.contains("zone_scroll_impression"))
		{
			try
			{
				bk.acknowledge(notification);
			}
			catch (Throwable e)
			{
				System.err.println(e.getMessage());
			}
		}

	}
}