package pt.com.gcs.messaging.adapt;

import pt.com.broker.types.NetBrokerMessage;
import pt.com.gcs.messaging.InternalMessage;

public class MessageTranslator
{
	public static InternalMessage translate(Message message)
	{
		InternalMessage intMsg = new InternalMessage();
		intMsg.setDestination(message.getDestination());
		intMsg.setContent( new NetBrokerMessage(message.getContent()) );
		intMsg.setPriority( message.getPriority() );
		intMsg.setExpiration(message.getExpiration() );
		intMsg.setTimestamp(message.getTimestamp());
		intMsg.setCorrelationId(message.getCorrelationId());
		intMsg.setFromRemotePeer(message.isFromRemotePeer());
		intMsg.setType(message.getType());		

		return intMsg;
	}
}
