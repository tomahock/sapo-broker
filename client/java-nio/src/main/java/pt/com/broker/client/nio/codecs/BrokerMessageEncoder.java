package pt.com.broker.client.nio.codecs;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.com.broker.types.BindingSerializer;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetProtocolType;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Created by luissantos on 21-04-2014.
 */
public class BrokerMessageEncoder extends MessageToByteEncoder<NetMessage> {



    private static final Logger log = LoggerFactory.getLogger(BrokerMessageEncoder.class);

    private BindingSerializer serializer;

    private NetProtocolType type;


    public BrokerMessageEncoder(NetProtocolType type) throws IllegalAccessException, InstantiationException, ClassNotFoundException {

        try {

            serializer = BindingSerializerFactory.getInstance(type);

        } catch (Exception e) {

            log.error("Was not possible to find the serializer");

            throw e;

        }

        this.type = type;
    }


    @Override
    protected void encode(ChannelHandlerContext ctx, NetMessage msg, ByteBuf out) throws Exception {

        byte[] data = serializer.marshal(msg);

        int size = data.length;

        short enc_type = getProtocolType(this.type);

        short enc_version = 0;


        out.writeShort(enc_type);
        out.writeShort(enc_version);
        out.writeInt(size);
        out.writeBytes(data);


    }

    protected short getProtocolType(NetProtocolType ptype) throws Exception{

        short proto_type = 0;

        switch (ptype)
        {
            case SOAP:
                proto_type = 0;
                break;
            case PROTOCOL_BUFFER:
                proto_type = 1;
                break;
            case THRIFT:
                proto_type = 2;
                break;
            case JSON:
                proto_type = 3;
                break;
            case SOAP_v0:
                proto_type = 0;
                break;
            default:
                throw new Exception("Invalid Protocol Type: " + ptype);
        }

        return proto_type;
    }





}
