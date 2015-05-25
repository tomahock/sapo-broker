package pt.com.broker.codec.xml;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

import pt.com.broker.types.BindingSerializer;
import pt.com.broker.types.NetMessage;

/**
 * Encoder implementation. Used to encode messages exchanged between client and agents.
 * 
 */

@ChannelHandler.Sharable
public class SoapEncoder extends MessageToMessageEncoder<NetMessage>
{
	private static final BindingSerializer serializer = new SoapBindingSerializer();

	@Override
	protected void encode(ChannelHandlerContext ctx, NetMessage msg, List out) throws Exception
	{

		out.add(serializer.marshal(msg));

	}

}