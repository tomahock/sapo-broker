package pt.com.protobuf.codec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.types.BindingSerializer;
import pt.com.types.Constants;
import pt.com.types.SimpleFramingDecoderV2;

public class ProtoBufDecoder extends SimpleFramingDecoderV2
{

	private static final Logger log = LoggerFactory.getLogger(ProtoBufDecoder.class);

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
