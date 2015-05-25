package pt.com.broker.codec.xml;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.io.InputStream;
import java.util.List;

import org.caudexorigo.ErrorAnalyser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.types.BindingSerializer;
import pt.com.broker.types.NetFault;
import pt.com.broker.types.NetMessage;

@ChannelHandler.Sharable
public class SoapDecoder extends MessageToMessageDecoder<ByteBuf>
{
	private static final Logger log = LoggerFactory.getLogger(SoapDecoder.class);

	private static final BindingSerializer serializer = new SoapBindingSerializer();

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception
	{

		Channel channel = ctx.channel();

		NetMessage message = null;
		try
		{
			InputStream in = new ByteBufInputStream(msg);
			message = serializer.unmarshal(in);
		}
		catch (Throwable t)
		{
			Throwable r = ErrorAnalyser.findRootCause(t);
			log.error(String.format("Message unmarshall failed: %s. Channel: '%s'", r.getMessage(), channel.remoteAddress().toString()));
		}

		if (message == null)
		{
			try
			{
				channel.write(NetFault.InvalidMessageFormatErrorMessage).addListener(ChannelFutureListener.CLOSE);
			}
			catch (Throwable t)
			{
				log.error("Failed to send 'InvalidMessageFormatErrorMessage'", t);
			}
		}

		out.add(message);

	}

}