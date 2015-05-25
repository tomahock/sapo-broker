package pt.com.broker.client.nio.codecs.oldframing;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.types.BindingSerializer;
import pt.com.broker.types.NetMessage;

/**
 * Created by luissantos on 21-04-2014.
 *
 * @author vagrant
 * @version $Id: $Id
 */
public class BrokerMessageDecoder extends ByteToMessageDecoder
{

	private static final Logger log = LoggerFactory.getLogger(BrokerMessageDecoder.class);

	private enum State
	{
		Header,
		Body
	}

	private State state = State.Header;

	private Integer bodyLen;

	private final BindingSerializer serializer;

	/**
	 * <p>
	 * Constructor for BrokerMessageDecoder.
	 * </p>
	 *
	 * @param serializer
	 *            a {@link pt.com.broker.types.BindingSerializer} object.
	 */
	public BrokerMessageDecoder(BindingSerializer serializer)
	{

		this.serializer = serializer;
	}

	/** {@inheritDoc} */
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception
	{

		switch (state)
		{

		case Header:
			if (in.readableBytes() < 4)
			{
				return;
			}

			decodeHeader(in);

			state = State.Body;
			break;

		case Body:

			if (in.readableBytes() < bodyLen)
			{
				return;
			}

			NetMessage msg = decodeBody(in);

			out.add(msg);

			state = State.Header;

			break;
		}

	}

	/**
	 * <p>
	 * decodeHeader.
	 * </p>
	 *
	 * @param in
	 *            a {@link io.netty.buffer.ByteBuf} object.
	 */
	protected void decodeHeader(ByteBuf in)
	{

		bodyLen = in.readInt();

	}

	/**
	 * <p>
	 * decodeBody.
	 * </p>
	 *
	 * @param in
	 *            a {@link io.netty.buffer.ByteBuf} object.
	 * @return a {@link pt.com.broker.types.NetMessage} object.
	 */
	protected NetMessage decodeBody(ByteBuf in)
	{

		byte[] body = new byte[bodyLen];

		in.readBytes(body);

		return serializer.unmarshal(body);

	}

}
