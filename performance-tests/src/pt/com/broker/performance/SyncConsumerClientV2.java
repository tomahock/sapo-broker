package pt.com.broker.performance;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import pt.com.broker.codec.protobuf.ProtoBufBindingSerializer;
import pt.com.broker.types.NetAcknowledge;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetAction.ActionType;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetNotification;
import pt.com.broker.types.NetPoll;

public class SyncConsumerClientV2 implements Runnable
{

	private AtomicInteger counter;
	private String clientId;
	private String host;
	private int port;
	private final String queueName;
	private final CountDownLatch countDown;

	private static AtomicInteger clientsEnded = new AtomicInteger(0);

	public SyncConsumerClientV2(AtomicInteger counter, String clientId, String host, int port, String queueName, CountDownLatch countDown)
	{
		super();
		this.counter = counter;
		this.clientId = clientId;
		this.host = host;
		this.port = port;
		this.queueName = queueName;
		this.countDown = countDown;
	}

	@Override
	public void run()
	{
		System.out.printf("SyncConsumerClient '%s' started%n", clientId);

		long pollTimeAcc = 0;
		int timeAccCount = 0;

		ProtoBufBindingSerializer serializer = new ProtoBufBindingSerializer();

		try
		{
			Socket client = new Socket(host, port);

			DataOutputStream rawo = new DataOutputStream(client.getOutputStream());

			DataInputStream rawi = new DataInputStream(client.getInputStream());

			NetPoll npoll = new NetPoll(queueName, -1);
			NetAction naction_poll = new NetAction(ActionType.POLL);
			naction_poll.setPollMessage(npoll);

			NetMessage nmsm = new NetMessage(naction_poll);

			byte[] poll_message = serializer.marshal(nmsm);

			while (counter.get() > 0)
			{

				long initNanoTime = System.nanoTime();

				// Send Poll
				rawo.writeShort(1);
				rawo.writeShort(0);
				rawo.writeInt(poll_message.length);
				rawo.write(poll_message);

				// Read ReceivedMessage

				short ptype = rawi.readShort();
				short pversion = rawi.readShort();
				int msize = rawi.readInt();

				byte[] rec_v_msg_b = new byte[msize];

				rawi.readFully(rec_v_msg_b);

				NetMessage rec_v_msg = serializer.unmarshal(rec_v_msg_b);

				NetAction action = rec_v_msg.getAction();

				if (action.getActionType() == ActionType.FAULT)
				{
					System.out.printf("Fault received. Code: '%s'%n", action.getFaultMessage().getCode());
					// break;
				}
				else if (action.getActionType() == ActionType.NOTIFICATION)
				{

					NetNotification poll = action.getNotificationMessage();

					String m_id = poll.getMessage().getMessageId();

					NetAcknowledge ack_message = new NetAcknowledge(queueName, m_id);
					NetAction naction_ack = new NetAction(ActionType.ACKNOWLEDGE);
					naction_ack.setAcknowledgeMessage(ack_message);

					NetMessage nmsm_ack = new NetMessage(naction_ack);

					byte[] ack_message_b = serializer.marshal(nmsm_ack);

					rawo.writeShort(1);
					rawo.writeShort(0);
					rawo.writeInt(ack_message_b.length);
					rawo.write(ack_message_b);

					long currentCount = counter.decrementAndGet();

					if ((currentCount % 50) == 0)
						System.out.println(currentCount);

				}
				else
				{
					System.err.println("Unknown message");
				}

				long endNanoTime = System.nanoTime();

				pollTimeAcc += (endNanoTime - initNanoTime);
				++timeAccCount;
			}

			client.close();

			double v = (double) ((pollTimeAcc / timeAccCount) / (1000 * 1000));

			if (timeAccCount != 0)
				System.out.printf("SyncConsumerClient '%s' ended. Average latency:%4.2fms %n", clientId, v);
			else
				System.out.printf("SyncConsumerClient '%s' ended. No messages received");

		}

		catch (Throwable ex)
		{
			ex.printStackTrace();

		}
		finally
		{

			// System.out.println("Clients that ended: " +clientsEnded.incrementAndGet());
			countDown.countDown();
		}
	}

}
