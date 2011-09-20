package pt.com.broker.codec.xml;

import java.io.InputStream;

import org.caudexorigo.ErrorAnalyser;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferInputStream;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandler.Sharable;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.types.BindingSerializer;
import pt.com.broker.types.NetFault;
import pt.com.broker.types.NetMessage;

@Sharable
public class SoapDecoder extends OneToOneDecoder
{
	private static final Logger log = LoggerFactory.getLogger(SoapDecoder.class);

	private static final BindingSerializer serializer = new SoapBindingSerializer();

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception
	{
		if (!(msg instanceof ChannelBuffer))
		{
			return msg;
		}

		NetMessage message = null;
		try
		{
			InputStream in = new ChannelBufferInputStream((ChannelBuffer) msg);
			message = serializer.unmarshal(in);
		}
		catch (Throwable t)
		{
			Throwable r = ErrorAnalyser.findRootCause(t);
			log.error(String.format("Message unmarshall failed: %s. Channel: '%s'", r.getMessage(), channel.getRemoteAddress().toString()));
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

		return message;
	}
}