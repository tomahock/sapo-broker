package pt.com.broker.codec.xml;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

public class NoFrammingSoapEncoder extends org.apache.mina.filter.codec.ProtocolEncoderAdapter
{

	@Override
	public void encode(IoSession ioSession, Object obj, ProtocolEncoderOutput out) throws Exception
	{
		// Do nothing

	}

}
