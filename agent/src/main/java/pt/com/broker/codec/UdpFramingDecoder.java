package pt.com.broker.codec;

import org.caudexorigo.ErrorAnalyser;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.com.broker.codec.protobuf.JsonCodecForProtoBuf;
import pt.com.broker.codec.protobuf.ProtoBufBindingSerializer;
import pt.com.broker.codec.thrift.ThriftBindingSerializer;
import pt.com.broker.codec.xml.SoapBindingSerializer;
import pt.com.broker.types.BindingSerializer;
import pt.com.broker.types.NetMessage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UdpFramingDecoder extends OneToOneDecoder
{
	private static final Logger log = LoggerFactory.getLogger(UdpFramingDecoder.class);

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

	public UdpFramingDecoder()
	{
		this(MAX_MESSAGE_SIZE);
	}

	public UdpFramingDecoder(int max_message_size)
	{
		super();
		_max_message_size = max_message_size;
	}

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel, Object arg) throws Exception
	{
		ChannelBuffer buffer = null;
		if (arg instanceof ChannelBuffer)
		{
			buffer = (ChannelBuffer) arg;
		}
		else
		{
			return null;
		}

		int readableBytes = buffer.readableBytes();

		if (readableBytes < HEADER_LENGTH)
		{
			return null;
		}

		if (readableBytes > _max_message_size)
		{
			log.error(String.format("Illegal message size!! Received message has %s bytes.", readableBytes));
			return null;
		}

		int mark = buffer.readerIndex();

		short protocol_type = buffer.getShort(mark);
		short protocol_version = buffer.getShort(mark + 2);
		int len = buffer.getInt(mark + 4);

		if (len > _max_message_size)
		{
			log.error(String.format("Illegal message size!! Received message claimed to have %s bytes.", len));

			return null;
		}
		else if (len <= 0)
		{
			log.error(String.format("Illegal message size!! Received message claimed to have %s bytes. Channel: '%s'", len, channel.getRemoteAddress().toString()));

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

		buffer.skipBytes(HEADER_LENGTH);

		byte[] decoded = new byte[len];
		buffer.readBytes(decoded);

		NetMessage nm = null;

		try
		{
			nm = serializer.unmarshal(decoded);
		}
		catch (Throwable t)
		{
			Throwable r = ErrorAnalyser.findRootCause(t);
			log.error("Failed to unmarshal message: '{}', payload: \n'{}'", r.getMessage(), new String(decoded));
		}

		return nm;
	}
}