package pt.com.broker.types;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

/**
 * Base class for encoding types. Implements MINA ProtocolEncoderAdapter, an abstract ProtocolEncoder implementation for those who don't have any resources to dispose.<br/>
 * Used by previous encoding schemes.
 * 
 */

public abstract class SimpleFramingEncoder extends ProtocolEncoderAdapter
{

	public void encode(IoSession session, Object message, ProtocolEncoderOutput pout) throws Exception
	{
		processBody(message, pout);
	}

	public abstract byte[] processBody(Object message);

	public abstract void processBody(Object message, ProtocolEncoderOutput pout);

}
