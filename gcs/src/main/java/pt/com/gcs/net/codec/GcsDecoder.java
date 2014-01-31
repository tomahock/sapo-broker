package pt.com.gcs.net.codec;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandler.Sharable;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.codec.protobuf.ProtoBufBindingSerializer;
import pt.com.broker.types.NetMessage;

/**
 * Encoder implementation. Used to encode messages exchanged between agents.
 * 
 * The wire message format is as simple as could be:
 * 
 * <pre>
 * ----------- 
 *  | Length  | -&gt; integer in network order: message:length
 *  -----------
 *  | Payload | -&gt; message payload
 *  -----------
 * </pre>
 * 
 * This applies to both input and ouput messages.
 */
@Sharable
public class GcsDecoder extends FrameDecoder
{

	private static final Logger log = LoggerFactory.getLogger(GcsDecoder.class);
	private static final int HEADER_LENGTH = 4;

	private final ProtoBufBindingSerializer serializer = new ProtoBufBindingSerializer();

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) throws Exception
	{
		int readableBytes = buffer.readableBytes();
		if (readableBytes < HEADER_LENGTH)
		{
			return null;
		}

		int mark = buffer.readerIndex();

		int len = buffer.getInt(mark);

		if (len <= 0)
		{
			log.error(String.format("Illegal message size!! Received message claimed to have %s bytes.", len));
			channel.close();
		}

		if (buffer.readableBytes() < (len + HEADER_LENGTH))
		{
			return null;
		}

		buffer.skipBytes(HEADER_LENGTH);

		byte[] decoded = new byte[len];
		buffer.readBytes(decoded);

		// ChannelBufferInputStream sIn = new ChannelBufferInputStream(buffer);

		try
		{
			NetMessage msg = serializer.unmarshal(decoded);
			return msg;
		}
		catch (Throwable e)
		{
			log.error(String.format("Can not unmarshall message"));
			channel.close();
			return null;
		}
	}

}