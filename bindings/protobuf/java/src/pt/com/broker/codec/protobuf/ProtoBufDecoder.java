package pt.com.broker.codec.protobuf;

import pt.com.broker.types.BindingSerializer;
import pt.com.broker.types.SimpleFramingDecoderV2;

/**
 * Google Protocol Buffer decoder.
 * 
 */

public class ProtoBufDecoder extends SimpleFramingDecoderV2
{

	private static final BindingSerializer serializer = new ProtoBufBindingSerializer();

	public ProtoBufDecoder(int maxMessageSize)
	{
		super(maxMessageSize);
	}

	@Override
	public Object processBody(byte[] packet, short protocolType, short protocolVersion)
	{
		return serializer.unmarshal(packet);
	}

}
