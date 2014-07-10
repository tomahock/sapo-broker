package pt.com.broker.codec;

import java.io.IOException;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import pt.com.broker.codec.xml.SoapBindingSerializer;
import pt.com.broker.types.NetMessage;

@ChannelHandler.Sharable
public class NoFramingEncoder extends MessageToMessageEncoder<NetMessage>
{
	private static final SoapBindingSerializer serializer = new SoapBindingSerializer();

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, NetMessage msg, List<Object> objects) throws Exception {

        try
        {
            byte[] bmsg = serializer.marshal(msg);

            ByteBuf out = channelHandlerContext.alloc().buffer(bmsg.length);
            out.writeBytes(bmsg);

            objects.add(out);
        }
        catch (Throwable t)
        {
            throw new IOException("Failed to encode message using NoFramingEncoder. Reason: " + t.getMessage());
        }

    }


}