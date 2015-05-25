package pt.com.broker.types;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Base class for encoding types. Implements MINA ProtocolEncoderAdapter, an abstract ProtocolEncoder implementation for those who don't have any resources to dispose.<br/>
 * Used by previous encoding schemes.
 * 
 */

public class SimpleFramingEncoder extends MessageToByteEncoder<byte[]>
{

	@Override
	protected void encode(ChannelHandlerContext ctx, byte[] msg, ByteBuf out) throws Exception
	{

		out.writeInt(msg.length);
		out.writeBytes(msg);

	}

}
