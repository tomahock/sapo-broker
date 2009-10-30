package pt.com.gcs.messaging;

import java.nio.charset.Charset;

import pt.com.broker.types.NetBrokerMessage;

public class InternalPublisher
{
	private static final Charset UTF8 = Charset.forName("UTF-8");

	public static void send(String destination_name, String content)
	{
		NetBrokerMessage brk_msg = new NetBrokerMessage(content.getBytes(UTF8));
		InternalMessage intr_msg = new InternalMessage();
		intr_msg.setContent(brk_msg);
		intr_msg.setDestination(destination_name);

		Gcs.publish(intr_msg);
	}

}
