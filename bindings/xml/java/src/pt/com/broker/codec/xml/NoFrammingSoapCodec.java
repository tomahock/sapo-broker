package pt.com.broker.codec.xml;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

public class NoFrammingSoapCodec implements ProtocolCodecFactory
{

	public static final int HEADER_LENGTH = 4;

	private NoFrammingSoapEncoder encoder;

	private NoFrammingSoapDecoder decoder;

	public NoFrammingSoapCodec()
	{
		encoder = new NoFrammingSoapEncoder();
		decoder = new NoFrammingSoapDecoder();
	}

	@Override
	public ProtocolDecoder getDecoder(IoSession arg0) throws Exception
	{
		return decoder;
	}

	@Override
	public ProtocolEncoder getEncoder(IoSession arg0) throws Exception
	{
		return encoder;
	}

}
