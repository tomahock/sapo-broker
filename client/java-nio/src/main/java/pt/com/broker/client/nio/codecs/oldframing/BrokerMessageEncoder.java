package pt.com.broker.client.nio.codecs.oldframing;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.com.broker.client.nio.codecs.BindingSerializerFactory;
import pt.com.broker.types.BindingSerializer;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetProtocolType;

/**
 * Created by luissantos on 21-04-2014.
 *
 * @author vagrant
 * @version $Id: $Id
 */
public class BrokerMessageEncoder extends MessageToByteEncoder<NetMessage> {


    private static final Logger log = LoggerFactory.getLogger(BrokerMessageEncoder.class);

    private final BindingSerializer serializer;

    protected boolean useFrame = true;


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


        if(getUseFrame()) {

            int size = data.length;
            out.writeInt(size);
        }

        out.writeBytes(data);


    }

    /**
     * <p>Getter for the field <code>useFrame</code>.</p>
     *
     * @return a boolean.
     */
    public boolean getUseFrame() {
        return useFrame;
    }

    /**
     * <p>Setter for the field <code>useFrame</code>.</p>
     *
     * @param useFrame a boolean.
     */
    public void setUseFrame(boolean useFrame) {
        this.useFrame = useFrame;
    }
}
