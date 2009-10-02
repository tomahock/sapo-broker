package pt.com.broker.codec.xml;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;


public class NoFrammingSoapDecoder extends org.apache.mina.filter.codec.ProtocolDecoderAdapter
{

	@Override
	public void decode(IoSession iosession, IoBuffer iobuffer, ProtocolDecoderOutput out) throws Exception
	{
		SoapEnvelope msg = SoapSerializer.FromXml(iobuffer.asInputStream());
		out.write(Builder.soapToNetMessage(msg));
	}

}
