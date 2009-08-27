package pt.com.broker.codec.thrift;

import org.apache.mina.core.buffer.IoBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.types.BindingSerializer;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.SimpleFramingEncoderV2;

/**
 * Thrift utility class for encoder.
 * 
 */
public class ThriftEncoder extends SimpleFramingEncoderV2
{

	private static final Logger log = LoggerFactory.getLogger(ThriftEncoder.class);

	private static final BindingSerializer serializer = new ThriftBindingSerializer();

	public ThriftEncoder()
	{

	}

	@Override
	public void processBody(Object message, IoBuffer wbuf, Short protocolType, Short protocolVersion)
	{
		if (!(message instanceof NetMessage))
		{
			// TODO: decide what to do with error
			log.error("Error encoding message.");
			return;
		}

		NetMessage gcsMessage = (NetMessage) message;
		wbuf.put(serializer.marshal(gcsMessage));
	}

}
