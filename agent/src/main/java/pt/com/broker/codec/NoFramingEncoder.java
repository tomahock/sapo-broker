package pt.com.broker.codec;

import java.io.IOException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandler.Sharable;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

import pt.com.broker.codec.xml.SoapBindingSerializer;
import pt.com.broker.types.NetMessage;

@Sharable
public class NoFramingEncoder extends OneToOneEncoder
{
	private static final SoapBindingSerializer serializer = new SoapBindingSerializer();

	@Override
	protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception
	{
		try
		{
			byte[] bmsg = serializer.marshal((NetMessage) msg);

			ChannelBuffer out = ChannelBuffers.buffer(bmsg.length);
			out.writeBytes(bmsg);

			return out;
		}
		catch (Throwable t)
		{
			throw new IOException("Failed to encode message using NoFramingEncoder. Reason: " + t.getMessage());
		}
	}
}