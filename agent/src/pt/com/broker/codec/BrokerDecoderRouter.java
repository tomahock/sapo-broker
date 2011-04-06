package pt.com.broker.codec;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.caudexorigo.ErrorAnalyser;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.codec.protobuf.JsonCodecForProtoBuf;
import pt.com.broker.codec.protobuf.ProtoBufBindingSerializer;
import pt.com.broker.codec.thrift.ThriftBindingSerializer;
import pt.com.broker.codec.xml.SoapBindingSerializer;
import pt.com.broker.types.BindingSerializer;
import pt.com.broker.types.NetFault;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.channels.ChannelAttributes;

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

public class BrokerDecoderRouter extends FrameDecoder
{
	private static final Logger log = LoggerFactory.getLogger(BrokerDecoderRouter.class);

	private final int _max_message_size;

	private static final int HEADER_LENGTH = 8;

	public static final int MAX_MESSAGE_SIZE = 256 * 1024;

	private static final Map<Short, BindingSerializer> decoders = new ConcurrentHashMap<Short, BindingSerializer>();

	static
	{
		decoders.put(new Short((short) 0), new SoapBindingSerializer());
		decoders.put(new Short((short) 1), new ProtoBufBindingSerializer());
		decoders.put(new Short((short) 2), new ThriftBindingSerializer());
		decoders.put(new Short((short) 3), new JsonCodecForProtoBuf());
	}

	public BrokerDecoderRouter()
	{
		this(MAX_MESSAGE_SIZE);
	}

	public BrokerDecoderRouter(int max_message_size)
	{
		super();
		_max_message_size = max_message_size;
	}

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) throws Exception
	{

		int readableBytes = buffer.readableBytes();
		if (readableBytes < HEADER_LENGTH)
		{
			return null;
		}

		int mark = buffer.readerIndex();

		short protocol_type = buffer.getShort(mark);
		short protocol_version = buffer.getShort(mark + 2);
		int len = buffer.getInt(mark + 4);

		if (len > _max_message_size)
		{
			log.error(String.format("Illegal message size!! Received message claimed to have %s bytes. Protocol Type: %s. Channel: '%s'", len, protocol_type, channel.getRemoteAddress().toString()));
			channel.write(NetFault.InvalidMessageSizeErrorMessage).addListener(ChannelFutureListener.CLOSE);

			return null;
		}
		else if (len <= 0)
		{
			log.error(String.format("Illegal message size!! Received message claimed to have %s bytes. Channel: '%s'", len, channel.getRemoteAddress().toString()));
			channel.write(NetFault.InvalidMessageSizeErrorMessage).addListener(ChannelFutureListener.CLOSE);

			return null;
		}

		if (buffer.readableBytes() < (len + HEADER_LENGTH))
		{
			return null;
		}

		BindingSerializer serializer = decoders.get(protocol_type);

		if (serializer == null)
		{
			log.error(String.format("Invalid protocol type:%s .Channel: '%s'", protocol_type, channel.getRemoteAddress().toString()));
			channel.write(ChannelBuffers.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
			return null;
		}

		ChannelAttributes.set(ChannelAttributes.getChannelId(ctx), "PROTOCOL_TYPE", new Short(protocol_type));
		ChannelAttributes.set(ChannelAttributes.getChannelId(ctx), "PROTOCOL_VERSION", new Short(protocol_version));

		buffer.skipBytes(HEADER_LENGTH);

		byte[] decoded = new byte[len];
		buffer.readBytes(decoded);

		NetMessage message = null;
		try
		{
			message = serializer.unmarshal(decoded);
		}
		catch (Throwable t)
		{
			Throwable r = ErrorAnalyser.findRootCause(t);
			log.error(String.format("Message unmarshall failed: %s. Serializer: '%s' Channel: '%s'", r.getMessage(), serializer.getClass().getCanonicalName(), channel.getRemoteAddress().toString()));
		}

		if (message == null)
		{
			try
			{
				channel.write(NetFault.InvalidMessageFormatErrorMessage).addListener(ChannelFutureListener.CLOSE);
			}
			catch (Throwable t)
			{
				log.error("Failed to send 'InvalidMessageFormatErrorMessage'", t);
			}
		}

		return message;
	}
}