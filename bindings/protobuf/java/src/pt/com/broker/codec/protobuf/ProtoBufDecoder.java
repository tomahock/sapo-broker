package pt.com.broker.codec.protobuf;

import java.io.InputStream;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferInputStream;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandler.Sharable;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;

import pt.com.broker.types.BindingSerializer;

/**
 * Google Protocol Buffer decoder.
 * 
 */
@Sharable
public class ProtoBufDecoder extends OneToOneDecoder
{

	private static final BindingSerializer serializer = new ProtoBufBindingSerializer();

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception
	{
		if (!(msg instanceof ChannelBuffer))
		{
			return msg;
		}

		InputStream in = new ChannelBufferInputStream((ChannelBuffer) msg);

		return serializer.unmarshal(in);
	}
}
