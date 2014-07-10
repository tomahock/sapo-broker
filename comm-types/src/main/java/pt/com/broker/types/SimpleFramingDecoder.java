package pt.com.broker.types;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Base class for decoding types. Extends Netty FrameDecoder that acumulates the content of received buffers to a cumulative buffer to help users implement decoders. <br/>
 * Used by previous encoding schemes.
 * 
 */

public class SimpleFramingDecoder extends ByteToMessageDecoder
{
	private static final Logger log = LoggerFactory.getLogger(SimpleFramingDecoder.class);

	private final int _max_message_size;

	public static final int HEADER_LENGTH = 4;

	public static final int MAX_MESSAGE_SIZE = 256 * 1024;

	public SimpleFramingDecoder()
	{
		this(MAX_MESSAGE_SIZE);
	}

	public SimpleFramingDecoder(int max_message_size)
	{
		super();
		_max_message_size = max_message_size;
	}

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) throws Exception {

        if (buffer.readableBytes() < HEADER_LENGTH)
        {
            return;
        }

        int mark = buffer.readerIndex();

        int len = buffer.getInt(mark);

        // Mark the current buffer position before reading the length field
        // because the whole frame might not be in the buffer yet.
        // We will reset the buffer position to the marked position if
        // there's not enough bytes in the buffer.

        // buffer.markReaderIndex();

        // The length field is in the buffer.
        // int len = buffer.readInt();

        if (buffer.readableBytes() < (len + HEADER_LENGTH))
        {
            // The whole bytes were not received yet - return null.
            // This method will be invoked again when more packets are
            // received and appended to the buffer.
            // Reset to the marked position to read the length field again
            // next time.
            return;
        }

        Channel channel = ctx.channel();

        if ((len <= 0) || (len > _max_message_size))
        {
            log.error(String.format("Illegal message size!! Received message claimed to have %s bytes. Channel: '%s'", len, channel.remoteAddress().toString()));
            channel.writeAndFlush(NetFault.InvalidMessageSizeErrorMessage).addListener(ChannelFutureListener.CLOSE);
            return;
        }

        buffer.skipBytes(HEADER_LENGTH);

        // There's enough bytes in the buffer. Read it.
        ByteBuf frame = buffer.readBytes(len);

        // Successfully decoded a frame. Return the decoded frame.
        out.add(frame);

    }


}