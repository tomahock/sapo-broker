package pt.com.protobuf.codec;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

import pt.com.types.Constants;


public class ProtoBufCodec implements ProtocolCodecFactory
{

	public static final int HEADER_LENGTH = 6;


	private ProtoBufEncoder encoder;
	private ProtoBufDecoder decoder;

	public ProtoBufCodec()
	{
		encoder = new ProtoBufEncoder();
		decoder = new ProtoBufDecoder(Constants.MAX_MESSAGE_SIZE);
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
