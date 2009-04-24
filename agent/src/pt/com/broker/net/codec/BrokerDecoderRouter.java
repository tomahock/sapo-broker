package pt.com.broker.net.codec;

import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.types.SimpleFramingDecoderV2;

public class BrokerDecoderRouter extends SimpleFramingDecoderV2
{

	private static final Logger log = LoggerFactory.getLogger(BrokerDecoderRouter.class);

	public BrokerDecoderRouter(int max_message_size)
	{
		super(max_message_size);
	}

	@Override
	public Object processBody(byte[] packet, short protocolType, short protocolVersion)
	{
		ProtocolCodecFactory codec = BrokerCodecRouter.getProcolCodec(new Short(protocolType));
		if (codec == null)
		{
			throw new RuntimeException("Invalid protocol type: " + protocolType);
		}
		SimpleFramingDecoderV2 decoder;
		try
		{
			decoder = (SimpleFramingDecoderV2) codec.getDecoder(null);
		}
		catch (Exception e)
		{
			throw new RuntimeException("Invalid protocol type decoder implementation: " + protocolType, e);
		}
		return decoder.processBody(packet, protocolType, protocolVersion);
	}

}
