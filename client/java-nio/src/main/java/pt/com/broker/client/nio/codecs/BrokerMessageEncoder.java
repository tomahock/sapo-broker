package pt.com.broker.client.nio.codecs;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
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
public class BrokerMessageEncoder extends MessageToByteEncoder<NetMessage> {



    private static final Logger log = LoggerFactory.getLogger(BrokerMessageEncoder.class);

    private final BindingSerializer serializer;


    /**
     * <p>Constructor for BrokerMessageEncoder.</p>
     *
     * @param serializer a {@link pt.com.broker.types.BindingSerializer} object.
     */
    public BrokerMessageEncoder(BindingSerializer serializer){

        this.serializer = serializer;
    }


    /** {@inheritDoc} */
    @Override
    protected void encode(ChannelHandlerContext ctx, NetMessage msg, ByteBuf out) throws Exception {

        byte[] data = serializer.marshal(msg);

        int size = data.length;

        short enc_type = getProtocolType();

        short enc_version = 0;


        out.writeShort(enc_type);
        out.writeShort(enc_version);
        out.writeInt(size);
        out.writeBytes(data);


    }

    /**
     * <p>getProtocolType.</p>
     *
     * @return a short.
     * @throws java.lang.Exception if any.
     */
    protected short getProtocolType() throws Exception{

        short proto_type = 0;


        switch (serializer.getProtocolType())
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
                throw new Exception("Invalid Protocol Type: " + serializer.getProtocolType());
        }

        return proto_type;
    }





}
