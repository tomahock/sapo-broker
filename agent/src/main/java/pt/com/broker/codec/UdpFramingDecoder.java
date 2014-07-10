package pt.com.broker.codec;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.caudexorigo.ErrorAnalyser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.codec.protobuf.JsonCodecForProtoBuf;
import pt.com.broker.codec.protobuf.ProtoBufBindingSerializer;
import pt.com.broker.codec.thrift.ThriftBindingSerializer;
import pt.com.broker.codec.xml.SoapBindingSerializer;
import pt.com.broker.types.BindingSerializer;
import pt.com.broker.types.NetMessage;

@ChannelHandler.Sharable()
public class UdpFramingDecoder extends MessageToMessageDecoder<DatagramPacket>
{
	private static final Logger log = LoggerFactory.getLogger(UdpFramingDecoder.class);

	private final int _max_message_size;

	private static final int HEADER_LENGTH = 8;

	public static final int MAX_MESSAGE_SIZE = 256 * 1024;

	private static final Map<Short, BindingSerializer> decoders = new ConcurrentHashMap<Short, BindingSerializer>();

	static
	{
		decoders.put(new Short((short) 0), new SoapBindingSerializer());
		decoders.put(new Short((short) 1), new ProtoBufBindingSerializer());
		decoders.put(new Short((short) 2), new ThriftBindingSerializer());
		decoders.put(new Short((short) 3), new JsonCodecForProtoBuf());
	}

	public UdpFramingDecoder()
	{
		this(MAX_MESSAGE_SIZE);
	}

	public UdpFramingDecoder(int max_message_size)
	{
		super();
		_max_message_size = max_message_size;
	}


    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext,  DatagramPacket packet, List<Object> objects) throws Exception {


        Channel channel = channelHandlerContext.channel();

        ByteBuf buffer = packet.content();

        int readableBytes = buffer.readableBytes();



        if (readableBytes < HEADER_LENGTH)
        {
            return;
        }

        if (readableBytes > _max_message_size)
        {
            log.error(String.format("Illegal message size!! Received message has %s bytes.", readableBytes));
            return;
        }

        int mark = buffer.readerIndex();

        short protocol_type = buffer.getShort(mark);
        short protocol_version = buffer.getShort(mark + 2);
        int len = buffer.getInt(mark + 4);

        if (len > _max_message_size)
        {
            log.error(String.format("Illegal message size!! Received message claimed to have %s bytes.", len));

            return;
        }
        else if (len <= 0)
        {
            log.error(String.format("Illegal message size!! Received message claimed to have %s bytes. Channel: '%s'", len, channel.remoteAddress().toString()));

            return;
        }

        if (buffer.readableBytes() < (len + HEADER_LENGTH))
        {
            return;
        }

        BindingSerializer serializer = decoders.get(protocol_type);

        if (serializer == null)
        {
            log.error(String.format("Invalid protocol type:%s .Channel: '%s'", protocol_type, channel.remoteAddress().toString()));
            channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
            return;
        }

        buffer.skipBytes(HEADER_LENGTH);

        byte[] decoded = new byte[len];
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