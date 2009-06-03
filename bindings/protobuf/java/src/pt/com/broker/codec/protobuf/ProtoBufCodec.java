package pt.com.broker.codec.protobuf;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

public class ProtoBufCodec implements ProtocolCodecFactory
{

	public static final int HEADER_LENGTH = 6;

	private ProtoBufEncoder encoder;
	private ProtoBufDecoder decoder;

	public ProtoBufCodec(int maxMessageSize)
	{
		encoder = new ProtoBufEncoder();
		decoder = new ProtoBufDecoder(maxMessageSize);
	}

	@Override
	public ProtocolDecoder getDecoder(IoSession session) throws Exception
	{
		return decoder;
	}

	@Override
	public ProtocolEncoder getEncoder(IoSession session) throws Exception
	{
		return encoder;
	}

}
