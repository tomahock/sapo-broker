package pt.com.gcs.net.codec;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferOutputStream;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandler.Sharable;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;
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
public class GcsEncoder extends OneToOneEncoder
{
	private static final Logger log = LoggerFactory.getLogger(GcsEncoder.class);

	private final ProtoBufBindingSerializer serializer = new ProtoBufBindingSerializer();

	@Override
	protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception
	{
		if (!(msg instanceof NetMessage))
		{
			String errorMessage = "Message to be encoded is from an unexpected type - " + msg.getClass().getName();
			log.error(errorMessage);
			throw new IllegalArgumentException(errorMessage);
		}

		ChannelBuffer out = ChannelBuffers.dynamicBuffer();
		ChannelBufferOutputStream sout = new ChannelBufferOutputStream(out);
		sout.writeInt(0);

		serializer.marshal((NetMessage) msg, sout);

		int len = out.writerIndex() - 4;

		out.setInt(0, len);

		return out;
	}
}