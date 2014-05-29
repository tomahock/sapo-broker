package pt.com.broker.client.nio.codecs;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.com.broker.types.BindingSerializer;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetProtocolType;

import java.util.List;

/**
 * Created by luissantos on 21-04-2014.
 */
public class BrokerMessageDecoder extends ByteToMessageDecoder{


    private static final Logger log = LoggerFactory.getLogger(BrokerMessageDecoder.class);

    private enum State{
        Header,
        Body
    }

    private State state = State.Header;

    private Integer bodyLen;

    private final BindingSerializer serializer;

    public BrokerMessageDecoder(BindingSerializer serializer){

        this.serializer = serializer;

    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {


        switch (state){

            case Header:
                if(in.readableBytes() < 8 ){
                    return;
                }

                decodeHeader(in);

                state = State.Body;
            break;

            case Body:

                if (in.readableBytes() < bodyLen ) {
                    return;
                }

                NetMessage msg = decodeBody(in);

                out.add(msg);



                state = State.Header;

            break;
        }


    }


    protected void decodeHeader(ByteBuf in){

        /* discard encoding type */
        in.readShort();

        /* discard encoding version */
        in.readShort();

        bodyLen = in.readInt();

    }

    protected NetMessage decodeBody(ByteBuf in){

       byte[] body = new byte[bodyLen];

       in.readBytes(body);

       return serializer.unmarshal(body);


    }

}
