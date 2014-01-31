package pt.com.broker.codec.thrift;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandler.Sharable;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;

import pt.com.broker.types.BindingSerializer;

/**
 * Thriftdecoder.
 * 
 */
@Sharable
public class ThriftDecoder extends OneToOneDecoder
{

	private static final BindingSerializer serializer = new ThriftBindingSerializer();

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception
	{
		if (!(msg instanceof ChannelBuffer))
		{
			return msg;
		}

		return serializer.unmarshal(((ChannelBuffer) msg).array());
	}
}