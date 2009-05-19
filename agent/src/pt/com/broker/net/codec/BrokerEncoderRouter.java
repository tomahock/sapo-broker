package pt.com.broker.net.codec;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.types.SimpleFramingEncoderV2;

public class BrokerEncoderRouter extends SimpleFramingEncoderV2
{

	private static final Logger log = LoggerFactory.getLogger(BrokerEncoderRouter.class);

	public BrokerEncoderRouter()
	{

	}

	// @Override
	// public byte[] processBody(Object message, Short protocolType, Short
	// protocolVersion)
	// {
	// ProtocolCodecFactory codec = BrokerCodecRouter.getProcolCodec(new
	// Short(protocolType));
	// if (codec == null)
	// {
	// throw new RuntimeException("Invalid protocol type: " + protocolType);
	// }
	//
	// SimpleFramingEncoderV2 encoder;
	// try
	// {
	// encoder = (SimpleFramingEncoderV2) codec.getEncoder(null);
	// }
	// catch (Exception e)
	// {
	// throw new
	// RuntimeException("Invalid protocol type decoder implementation: " +
	// protocolType, e);
	// }
	// return encoder.processBody(message, protocolType, protocolVersion);
	// }

	@Override
	public void processBody(Object message, IoBuffer wbuf, Short protocolType, Short protocolVersion)
	{
		ProtocolCodecFactory codec = BrokerCodecRouter.getProcolCodec(new Short(protocolType));
		if (codec == null)
		{
			throw new RuntimeException("Invalid protocol type: " + protocolType);
		}

		SimpleFramingEncoderV2 encoder;
		try
		{
			encoder = (SimpleFramingEncoderV2) codec.getEncoder(null);
		}
		catch (Exception e)
		{
			throw new RuntimeException("Invalid protocol type decoder implementation: " + protocolType, e);
		}

		encoder.processBody(message, wbuf, protocolType, protocolVersion);
	}
}
