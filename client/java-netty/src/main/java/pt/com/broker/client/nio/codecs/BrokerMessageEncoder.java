package pt.com.broker.client.nio.codecs;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.com.broker.types.BindingSerializer;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetProtocolType;

import java.util.List;

/**
 * Created by luissantos on 21-04-2014.
 */
public class BrokerMessageEncoder extends MessageToByteEncoder<NetMessage> {



    private static final Logger log = LoggerFactory.getLogger(BrokerMessageEncoder.class);

    private BindingSerializer serializer;

    public BrokerMessageEncoder(NetProtocolType type) throws IllegalAccessException, InstantiationException, ClassNotFoundException {

        try {

            serializer = BindingSerializerFactory.getInstance(type);

        } catch (Exception e) {

            log.error("Was not possible to find the serializer");

            throw e;

        }
    }


    @Override
    protected void encode(ChannelHandlerContext ctx, NetMessage msg, ByteBuf out) throws Exception {

    }
}
