package pt.com.broker.types;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

/**
 * Base class for encoding types. Implements MINA ProtocolEncoderAdapter, an abstract ProtocolEncoder implementation for those who don't have any resources to dispose.<br/>
 * Used by previous encoding schemes.
 * 
 */

public class SimpleFramingEncoder extends OneToOneEncoder
{

	@Override
	protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception
	{
		if (!(msg instanceof byte[]))
		{
			return null;
		}

		byte[] bmsg = (byte[]) msg;

		ChannelBuffer out = ChannelBuffers.buffer(bmsg.length + 4);
		out.writeInt(bmsg.length);
		out.writeBytes(bmsg);

		return out;
	}
}
