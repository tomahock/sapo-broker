package pt.com.types;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

public abstract class SimpleFramingEncoder extends ProtocolEncoderAdapter
{
	public void encode(IoSession session, Object message, ProtocolEncoderOutput pout) throws Exception
	{
		Short protocolType = (Short) session.getAttribute("PROTOCOL_TYPE");
		Short protocolVersion = (Short) session.getAttribute("PROTOCOL_VERSION");
		
		processBody(message, pout, protocolType, protocolVersion);
	}

	public abstract byte[] processBody(Object message, Short protocolType, Short protocolVersion);

	public abstract void processBody(Object message, ProtocolEncoderOutput pout, Short protocolType, Short protocolVersion);

}
