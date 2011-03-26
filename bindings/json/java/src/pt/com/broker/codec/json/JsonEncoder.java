package pt.com.broker.codec.json;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandler.Sharable;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

import pt.com.broker.codec.protobuf.JsonCodecForProtoBuf;
import pt.com.broker.types.BindingSerializer;
import pt.com.broker.types.NetMessage;

/**
 * Json Buffer encoder.
 * 
 */
@Sharable
public class JsonEncoder extends OneToOneEncoder
{
	private static final BindingSerializer serializer = new JsonCodecForProtoBuf();

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