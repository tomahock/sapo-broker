package pt.com.broker.net.codec;

import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.types.SimpleFramingDecoder;

public class BrokerDecoderRouter extends SimpleFramingDecoder
{

	private static final Logger log = LoggerFactory.getLogger(BrokerDecoderRouter.class);

	public BrokerDecoderRouter(int max_message_size)
	{
		super(max_message_size, true);
	}

	@Override
	public Object processBody(byte[] packet, short protocolType, short protocolVersion)
	{
		ProtocolCodecFactory codec = BrokerCodecRouter.getProcolCodec(new Short(protocolType));
		if (codec == null)
		{
			throw new RuntimeException("Invalid protocol type: " + protocolType);
		}

		SimpleFramingDecoder decoder;
		try
		{
			decoder = (SimpleFramingDecoder) codec.getDecoder(null);
		}
		catch (Exception e)
		{
			throw new RuntimeException("Invalid protocol type decoder implementation: " + protocolType, e);
		}
		return decoder.processBody(packet, protocolType, protocolVersion);
	}

}
