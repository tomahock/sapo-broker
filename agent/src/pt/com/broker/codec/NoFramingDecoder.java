package pt.com.broker.codec;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.codec.xml.SoapBindingSerializer;
import pt.com.broker.types.NetFault;
import pt.com.broker.types.NetMessage;

public class NoFramingDecoder extends OneToOneDecoder
{
	private static final Logger log = LoggerFactory.getLogger(NoFramingDecoder.class);

	private final int _max_message_size;

	public static final int MAX_MESSAGE_SIZE = 256 * 1024;

	private static final SoapBindingSerializer serializer = new SoapBindingSerializer();

	public NoFramingDecoder()
	{
		this(MAX_MESSAGE_SIZE);
	}

	public NoFramingDecoder(int max_message_size)
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

		if (readableBytes > _max_message_size)
		{
			log.error(String.format("Illegal message size!! Received message has %s bytes.", readableBytes));

			channel.write(NetFault.InvalidMessageSizeErrorMessage).addListener(ChannelFutureListener.CLOSE);

			return null;
		}

		byte[] decoded = new byte[readableBytes];
		buffer.readBytes(decoded);

		NetMessage nm = null;

		try
		{
			nm = serializer.unmarshal(decoded);
		}
		catch (Throwable t)
		{
			log.error("Failed to unmarshal message", t);
		}

		return nm;
	}
}