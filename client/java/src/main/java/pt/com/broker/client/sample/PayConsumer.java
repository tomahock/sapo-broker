package pt.com.broker.client.sample;

import org.caudexorigo.Shutdown;
import org.caudexorigo.concurrent.Sleep;
import org.caudexorigo.io.NullOutputWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.com.broker.client.BrokerClient;
import pt.com.broker.client.messaging.BrokerListener;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.broker.types.NetNotification;
import pt.com.broker.types.NetSubscribe;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class PayConsumer implements BrokerListener
{
	private static final Logger log = LoggerFactory.getLogger(PayConsumer.class);
	private static final NullOutputWriter dev_null = new NullOutputWriter();

	private final DestinationType dtype;
	private final long waitTime;
	private BrokerClient bk;
	private int msgCount;

	public PayConsumer(String host, int port, String dname, long waitTime, int msgCount)
	{

		this.dtype = DestinationType.QUEUE;
		this.waitTime = waitTime;
		this.msgCount = msgCount;

		try
		{
			bk = new BrokerClient(host, port);
			NetSubscribe subscribe = new NetSubscribe(dname, dtype);
			bk.addAsyncConsumer(subscribe, this);
		}
		catch (Throwable t)
		{
			Shutdown.now(t);
		}
	}

	@Override
	public boolean isAutoAck()
	{
		return false;
	}

	@Override
	public void onMessage(NetNotification notification)
	{
		try
		{
			log.info("Received message: {}", new String(notification.getMessage().getPayload()));

			if (waitTime > 0)
			{
				Sleep.time(waitTime);
			}

			msgCount--;

			bk.acknowledge(notification);

			if (msgCount == 0)
			{
				bk.unsubscribe(dtype, notification.getDestination());
				deleteQueue(notification.getDestination());
				bk.close();
			}
		}
		catch (Throwable t)
		{
			log.error(t.getMessage());
		}
	}

	private void deleteQueue(String queueName)
	{
		try
		{
			String endpoint = "http://localhost:3380/broker/admin";

			String delete = String.format("QUEUE:%s", queueName);

			System.out.println("PayConsumer.deleteQueue: " + delete);

			URL url = new URL(endpoint);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");

			con.setRequestProperty("Content-length", String.valueOf(delete.length()));
			con.setRequestProperty("Content-Type", "");
			con.setDoOutput(true);
			con.setDoInput(true);

			DataOutputStream output = new DataOutputStream(con.getOutputStream());

			output.writeBytes(delete);
			output.flush();

			output.close();

			DataInputStream input = new DataInputStream(con.getInputStream());

			for (int c = input.read(); c != -1; c = input.read())
				System.out.print((char) c);
			input.close();

			// dev_null.write(c);

			// System.out.println("Resp Code:" + con.getResponseCode());
			// System.out.println("Resp Message:" + con.getResponseMessage());
		}
		catch (Throwable t)
		{
			log.error(t.getMessage());
		}
	}
}