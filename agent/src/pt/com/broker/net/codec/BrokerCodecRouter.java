package pt.com.broker.net.codec;

import java.util.HashMap;
import java.util.Map;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

import pt.com.broker.codec.protobuf.ProtoBufCodec;
import pt.com.broker.codec.thrift.ThriftCodec;
import pt.com.broker.codec.xml.SoapCodecV2;
import pt.com.broker.types.Constants;

/**
 * The network protocol has the following layout:
 * 
 * <pre>
 *  -----------
 *  |  Type   | -&gt; 16-bit signed integer in network order for protocol type
 *  -----------
 *  | Version | -&gt; 16-bit signed integer in network order for protocol version
 *  ----------- 
 *  | Length  | -&gt; 32-bit signed integer in network order for the payload length
 *  -----------
 *  | Payload | -&gt; binary message
 *  -----------
 * </pre>
 * 
 * This applies to both input and output messages.
 */
public class BrokerCodecRouter implements ProtocolCodecFactory
{

	static private Map<Short, ProtocolCodecFactory> codecs = new HashMap<Short, ProtocolCodecFactory>();

	static
	{
		codecs.put(new Short((short) 0), new SoapCodecV2());
		codecs.put(new Short((short) 1), new ProtoBufCodec());
		codecs.put(new Short((short) 2), new ThriftCodec());
	}

	// TODO: Create a constructor that specifies this value. The original value
	// was defined by: Constants.MAX_MESSAGE_SIZE

	private static BrokerCodecRouter instance = new BrokerCodecRouter();

	public static BrokerCodecRouter getInstance()
	{
		return instance;
	}

	private BrokerEncoderRouter encoder;
	private BrokerDecoderRouter decoder;

	public BrokerCodecRouter()
	{
		encoder = new BrokerEncoderRouter();
		decoder = new BrokerDecoderRouter(Constants.MAX_MESSAGE_SIZE);
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
