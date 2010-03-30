package pt.com.gcs.messaging;

import java.nio.charset.Charset;

import pt.com.broker.types.NetBrokerMessage;
import pt.com.broker.types.NetPublish;
import pt.com.broker.types.NetAction.DestinationType;

public class InternalPublisher
{
	private static final Charset UTF8 = Charset.forName("UTF-8");

	public static void send(String destination_name, String content)
	{
		NetBrokerMessage brk_msg = new NetBrokerMessage(content.getBytes(UTF8));		
		NetPublish np = new NetPublish(destination_name, DestinationType.TOPIC, brk_msg);
		Gcs.publish(np);
	}

}
