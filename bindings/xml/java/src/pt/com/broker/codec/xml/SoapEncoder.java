package pt.com.broker.codec.xml;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandler.Sharable;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

import pt.com.broker.types.BindingSerializer;
import pt.com.broker.types.NetMessage;

/**
 * Encoder implementation. Used to encode messages exchanged between client and agents.
 * 
 */

@Sharable
public class SoapEncoder extends OneToOneEncoder
{
	private static final BindingSerializer serializer = new SoapBindingSerializer();

	@Override
	protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception
	{
		if (!(msg instanceof NetMessage))
		{
			return msg;
		}

		return serializer.marshal((NetMessage) msg);
	}
}