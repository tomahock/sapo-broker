package pt.com.gcs.net.codec;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.caudexorigo.io.UnsynchByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.types.SimpleFramingEncoder;
import pt.com.gcs.io.SerializerHelper;
import pt.com.gcs.messaging.InternalMessage;

/**
 * Encoder implementation. Used to encode messages exchanged between agents.
 * 
 */

public class GcsEncoder extends SimpleFramingEncoder
{
	private static final Logger log = LoggerFactory.getLogger(GcsEncoder.class);

	@Override
	public byte[] processBody(Object message)
	{
		if (!(message instanceof InternalMessage))
		{
			String errorMessage = "Message to be encoded is from an unexpected type - " + message.getClass().getName();
			log.error(errorMessage);
			throw new IllegalArgumentException(errorMessage);
		}
		UnsynchByteArrayOutputStream holder = new UnsynchByteArrayOutputStream();
		SerializerHelper.toStream((InternalMessage) message, holder);

		return holder.toByteArray();
	}

	@Override
	public void processBody(Object message, ProtocolEncoderOutput pout)
	{

		if (!(message instanceof InternalMessage))
		{
			String errorMessage = "Message to be encoded is from an unexpected type - " + message.getClass().getName();
			log.error(errorMessage);
			throw new IllegalArgumentException(errorMessage);
		}

		IoBuffer wbuf = IoBuffer.allocate(2048, false);
		wbuf.setAutoExpand(true);
		wbuf.putInt(0);
		SerializerHelper.toStream((InternalMessage) message, wbuf.asOutputStream());
		int msize = wbuf.position() - 4;
		wbuf.putInt(0, msize);

		wbuf.flip();

		pout.write(wbuf);
	}

}