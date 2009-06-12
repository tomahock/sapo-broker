package pt.com.broker.codec.thrift;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.types.BindingSerializer;
import pt.com.broker.types.SimpleFramingDecoderV2;

/**
 * Thriftdecoder. 
 *
 */
public class ThriftDecoder extends SimpleFramingDecoderV2
{

	private static final Logger log = LoggerFactory.getLogger(ThriftDecoder.class);

	private static final BindingSerializer serializer = new ThriftBindingSerializer();

	public ThriftDecoder(int maxMessageSize)
	{
		super(maxMessageSize);
	}

	@Override
	public Object processBody(byte[] packet, short protocolType, short protocolVersion)
	{
		return serializer.unmarshal(packet);
	}

}