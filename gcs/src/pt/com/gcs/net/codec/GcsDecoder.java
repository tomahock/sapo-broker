package pt.com.gcs.net.codec;

import org.caudexorigo.io.UnsynchronizedByteArrayInputStream;

import pt.com.broker.types.SimpleFramingDecoder;
import pt.com.gcs.io.SerializerHelper;

/**
 * Encoder implementation. Used to encode messages exchanged between agents.
 * 
 */

public class GcsDecoder extends SimpleFramingDecoder
{
	public GcsDecoder(int max_message_size)
	{
		super(max_message_size);
	}

	@Override
	public Object processBody(byte[] packet)
	{
		UnsynchronizedByteArrayInputStream bin = new UnsynchronizedByteArrayInputStream(packet);
		Object msg = SerializerHelper.fromStream(bin);
		
		return msg;
	}
}
