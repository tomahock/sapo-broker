package pt.com.broker.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

import org.caudexorigo.ErrorAnalyser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.codec.xml.SoapBindingSerializer;
import pt.com.broker.types.NetMessage;

public class NoFramingDecoder extends MessageToMessageDecoder<DatagramPacket>
{
	private static final Logger log = LoggerFactory.getLogger(NoFramingDecoder.class);

	private final int _max_message_size;

	public static final int MAX_MESSAGE_SIZE = 256 * 1024;

	private static final SoapBindingSerializer serializer = new SoapBindingSerializer();

	public NoFramingDecoder()
	{
		this(MAX_MESSAGE_SIZE);
	}

	public NoFramingDecoder(int max_message_size)
	{
		super();
		_max_message_size = max_message_size;
	}

	@Override
	protected void decode(ChannelHandlerContext channelHandlerContext, DatagramPacket packet, List<Object> objects) throws Exception
	{

		ByteBuf buffer = packet.content();

		int readableBytes = buffer.readableBytes();

		if ((readableBytes > _max_message_size) || (readableBytes <= 0))
		{
			log.error(String.format("Illegal message size!! Received message has %s bytes.", readableBytes));
			return;
		}

		byte[] decoded = new byte[readableBytes];

		buffer.readBytes(decoded);

		NetMessage nm = null;

		try
		{
			nm = serializer.unmarshal(decoded);
			objects.add(nm);
		}
		catch (Throwable t)
		{
			Throwable r = ErrorAnalyser.findRootCause(t);
			log.error("Failed to unmarshal message: '{}', payload: \n'{}'", r.getMessage(), new String(decoded));
		}

	}

}