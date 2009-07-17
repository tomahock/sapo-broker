package pt.com.broker.types;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

/**
 * Base class for encoding types. Implements MINA ProtocolEncoderAdapter, an abstract ProtocolEncoder implementation for those who don't have any resources to dispose.<br/> 
 * Adds wire message header - Message length, encoding type and encoding version.
 *
 */

public abstract class SimpleFramingEncoderV2 extends ProtocolEncoderAdapter
{
	public void encode(IoSession session, Object message, ProtocolEncoderOutput pout) throws Exception
	{
		Short protocolType = (Short) session.getAttribute("PROTOCOL_TYPE");
		Short protocolVersion = (Short) session.getAttribute("PROTOCOL_VERSION");

		IoBuffer wbuf = IoBuffer.allocate(2048, false);
		wbuf.setAutoExpand(true);
		wbuf.putShort(protocolType.shortValue());
		wbuf.putShort(protocolVersion.shortValue());
		wbuf.putInt(0); // placeholder

		processBody(message, wbuf, protocolType, protocolVersion);
		int len = wbuf.position() - 8;
		//TODO: if len equal zero serialization failed. Deal with this.
		wbuf.putInt(4, len);
		wbuf.flip();
		pout.write(wbuf);
	}

	public abstract void processBody(Object message, IoBuffer wbuf, Short protocolType, Short protocolVersion);

}
