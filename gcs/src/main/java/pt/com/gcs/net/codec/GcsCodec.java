package pt.com.gcs.net.codec;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import pt.com.broker.codec.protobuf.ProtoBufBindingSerializer;
import pt.com.broker.types.NetMessage;

/**
 * The wire message format is as simple as could be:
 * 
 * <pre>
 * ----------- 
 *  | Length  | -&gt; integer in network order: message:length
 *  -----------
 *  | Payload | -&gt; message payload
 *  -----------
 * </pre>
 * 
 * This applies to both input and ouput messages.
 */

public class GcsCodec extends ByteToMessageCodec<NetMessage> {
	
	private static final Logger log = LoggerFactory.getLogger(GcsCodec.class);
	
	public static final int HEADER_LENGTH = 4;
	private final ProtoBufBindingSerializer serializer = new ProtoBufBindingSerializer();
	
	@Override
	protected void encode(ChannelHandlerContext ctx, NetMessage msg, ByteBuf out)
			throws Exception {
		byte[] buffer = serializer.marshal(msg);
		out.writeInt(buffer.length);
		out.writeBytes(buffer);
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in,
			List<Object> out) throws Exception {
		int readableBytes = in.readableBytes();
        if(readableBytes >= HEADER_LENGTH){
        	int len = in.readInt();
        	if(len <= 0){
        		log.error(String.format("Illegal message size!! Received message claimed to have %s bytes.", len));
                ctx.close();
        	}
        	if(readableBytes >= (len + HEADER_LENGTH)){
        		//TODO: Check if this is the best possible approach. No buffer limit?!?!
        		byte[] decoded = new byte[len];
                in.readBytes(decoded);
                NetMessage msg = serializer.unmarshal(decoded);
                out.add(msg);
        	}
        }
		
	}
	
}
