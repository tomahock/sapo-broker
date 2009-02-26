package pt.com.broker.net.codec;

import java.util.HashMap;
import java.util.Map;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

import pt.com.protobuf.codec.ProtoBufCodec;
import pt.com.thrift.codec.ThriftCodec;
import pt.com.xml.codec.SoapCodec;

/**
 * The network protocol has the following layout:
 * 
 * <pre>
 *  ----------- 
 *  | Length  | -&gt; integer in network order (msb = 0)
 *  -----------
 *  | Payload | -&gt; binary message
 *  -----------
 * </pre>
 * 
 * or
 * 
 * <pre>
 *  ----------- 
 *  | Length  | -&gt; integer in network order (msb = 1) [32 bits]
 *  -----------
 *  |  Type   | -&gt; protocol type [16 bits]
 *  -----------
 *  | Version | -&gt; protocol version [16 bits] 
 *  -----------
 *  | Payload | -&gt; binary message
 *  -----------
 * </pre>
 * 
 * The most significant bit (msb) determines if the message length is followed by the payload (msb = 0), maintaining retro-compatibility, or the protocol type and version (msb = 1). <br/>
 * This applies to both input and output messages.
 */
public class BrokerCodecRouter implements ProtocolCodecFactory
{

	static private Map<Short, ProtocolCodecFactory> codecs = new HashMap<Short, ProtocolCodecFactory>();

	static
	{
		codecs.put(new Short((short) 1), new ProtoBufCodec());
		codecs.put(new Short((short) 2), new ThriftCodec());
	}

	// TODO: Create a constructor that specifies this value. The original value was defined by: MQ.MAX_MESSAGE_SIZE
	public static final int MAX_MESSAGE_SIZE = 4 * 1024;

	private BrokerEncoderRouter encoder;
	private BrokerDecoderRouter decoder;

	public BrokerCodecRouter()
	{
		encoder = new BrokerEncoderRouter();
		decoder = new BrokerDecoderRouter(MAX_MESSAGE_SIZE);
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

	static public ProtocolCodecFactory getProcolCodec(Short protocolVersion)
	{
		return codecs.get(protocolVersion);
	}

}
