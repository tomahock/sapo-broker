package pt.com.broker.client.nio.bootstrap;

import io.netty.channel.Channel;
import pt.com.broker.client.nio.codecs.oldframing.BrokerMessageEncoder;
import pt.com.broker.types.NetProtocolType;

/**
 * Created by luissantos on 06-05-2014.
 */
public class DatagramChannelInitializer extends BaseChannelInitializer {

    public DatagramChannelInitializer(NetProtocolType protocolType) {
        super(protocolType);

        if(! (protocolType == NetProtocolType.PROTOCOL_BUFFER || protocolType == NetProtocolType.THRIFT) ){
            log.warn("Using non-binary encoding with datagram transport will add some overhead ");
        }
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {

        super.initChannel(ch);

        if(isOldFraming()){

            BrokerMessageEncoder encoder =  (BrokerMessageEncoder) ch.pipeline().get("broker_message_encoder");
            encoder.setUseFrame(false);
        }


    }
}
