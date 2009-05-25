package pt.com.broker.codec.protobuf;

import pt.com.broker.types.BindingSerializer;
import pt.com.broker.types.Constants;
import pt.com.broker.types.SimpleFramingDecoderV2;

public class ProtoBufDecoder extends SimpleFramingDecoderV2
{

	private static final BindingSerializer serializer = new ProtoBufBindingSerializer();

	public ProtoBufDecoder()
	{
		super(Constants.MAX_MESSAGE_SIZE);
	}

	@Override
	public Object processBody(byte[] packet, short protocolType, short protocolVersion)
	{
		return serializer.unmarshal(packet);
	}

}
