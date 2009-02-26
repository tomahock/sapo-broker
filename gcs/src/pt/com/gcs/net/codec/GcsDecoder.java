package pt.com.gcs.net.codec;

import org.caudexorigo.io.UnsynchByteArrayInputStream;

import pt.com.gcs.io.SerializerHelper;
import pt.com.types.SimpleFramingDecoder;

public class GcsDecoder extends SimpleFramingDecoder
{
	public GcsDecoder(int max_message_size)
	{
		super(max_message_size, false);
	}

	@Override
	public Object processBody(byte[] packet, short protocolType, short protocolVersion)
	{
		UnsynchByteArrayInputStream bin = new UnsynchByteArrayInputStream(packet);
		Object msg = SerializerHelper.fromStream(bin);
		return msg;
	}
}
