package pt.com.gcs.net.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.com.broker.codec.protobuf.ProtoBufBindingSerializer;
import pt.com.broker.types.NetMessage;

import java.util.List;

/**
 * Encoder implementation. Used to encode messages exchanged between agents.
 * 
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
 * @todo ver Sharable
 */
//@ChannelHandler.Sharable
public class GcsDecoder extends ByteToMessageDecoder {

	private static final Logger log = LoggerFactory.getLogger(GcsDecoder.class);
	private static final int HEADER_LENGTH = 4;

	private final ProtoBufBindingSerializer serializer = new ProtoBufBindingSerializer();

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) throws Exception {
        int readableBytes = buffer.readableBytes();
        if(readableBytes >= HEADER_LENGTH){
        	int len = buffer.readInt();
        	if(len <= 0){
        		log.error(String.format("Illegal message size!! Received message claimed to have %s bytes.", len));
                ctx.close();
        	}
        	if(readableBytes >= (len + HEADER_LENGTH)){
        		//TODO: Check if this is the best possible approach. No buffer limit?!?!
        		byte[] decoded = new byte[len];
                buffer.readBytes(decoded);
                NetMessage msg = serializer.unmarshal(decoded);
                out.add(msg);
        	}
        }
    }

}