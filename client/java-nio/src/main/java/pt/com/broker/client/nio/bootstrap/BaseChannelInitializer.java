package pt.com.broker.client.nio.bootstrap;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.MessageToByteEncoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.client.nio.codecs.BrokerMessageDecoder;
import pt.com.broker.client.nio.codecs.BrokerMessageEncoder;
import pt.com.broker.types.BindingSerializer;

/**
 * Created by luissantos on 05-05-2014.
 *
 * @author vagrant
 * @version $Id: $Id
 */
public abstract class BaseChannelInitializer extends io.netty.channel.ChannelInitializer<Channel>
{

	/** Constant <code>log</code> */
	protected static final Logger log = LoggerFactory.getLogger(BaseChannelInitializer.class);

	protected final BindingSerializer serializer;

	private boolean oldFraming = false;

	/**
	 * <p>
	 * Constructor for BaseChannelInitializer.
	 * </p>
	 *
	 * @param serializer
	 *            a {@link pt.com.broker.types.BindingSerializer} object.
	 */
	public BaseChannelInitializer(BindingSerializer serializer)
	{
		this.serializer = serializer;
	}

	/** {@inheritDoc} */
	@Override
	protected void initChannel(Channel ch) throws Exception
	{

		ChannelPipeline pipeline = ch.pipeline();

		if (isOldFraming())
		{

			/* add Message <> byte encode decoder */
			pipeline.addLast("broker_message_decoder", new pt.com.broker.client.nio.codecs.oldframing.BrokerMessageDecoder(serializer));
			pipeline.addLast("broker_message_encoder", new pt.com.broker.client.nio.codecs.oldframing.BrokerMessageEncoder(serializer));

		}
		else
		{

			/* add Message <> byte encode decoder */
			pipeline.addLast("broker_message_decoder", new BrokerMessageDecoder(serializer));
			pipeline.addLast("broker_message_encoder", new BrokerMessageEncoder(serializer));
		}

		ch.pipeline().addLast("byte_message_encoder", new MessageToByteEncoder<Byte[]>()
		{

			@Override
			protected void encode(ChannelHandlerContext ctx, Byte[] msg, ByteBuf out) throws Exception
			{

				byte[] data = new byte[msg.length];

				int pos = 0;
				for (Byte bye : msg)
				{
					data[pos++] = bye.byteValue();
				}

				out.writeBytes(data);
			}
		});
	}

	/**
	 * <p>
	 * isOldFraming.
	 * </p>
	 *
	 * @return a boolean.
	 */
	protected boolean isOldFraming()
	{
		return oldFraming;
	}

	/**
	 * <p>
	 * Setter for the field <code>oldFraming</code>.
	 * </p>
	 *
	 * @param oldFraming
	 *            a boolean.
	 */
	public void setOldFraming(boolean oldFraming)
	{
		this.oldFraming = oldFraming;
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception
	{
		log.debug("********Unexpected exception caught.*********", cause);
	}

}
