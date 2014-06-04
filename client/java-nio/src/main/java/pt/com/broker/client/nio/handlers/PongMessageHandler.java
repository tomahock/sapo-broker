package pt.com.broker.client.nio.handlers;

import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.com.broker.client.nio.consumer.ConsumerManager;
import pt.com.broker.client.nio.consumer.PongConsumerManager;
import pt.com.broker.client.nio.utils.ChannelDecorator;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetBrokerMessage;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetPublish;

import java.net.InetSocketAddress;


/**
 * Created by luissantos on 22-04-2014.
 */
@ChannelHandler.Sharable
public class PongMessageHandler extends SimpleChannelInboundHandler<NetMessage> {

    public static final String HEART_BEAT_ACTION_ID = "24bb963d-6d6c-441e-ab4d-999d73578452";

    private static final Logger log = LoggerFactory.getLogger(PongMessageHandler.class);

    PongConsumerManager manager;

    public PongMessageHandler(PongConsumerManager manager) {
        super();

        this.manager = manager;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, NetMessage msg){

        if(ctx==null){
            return;
        }

        try {


            NetAction action = msg.getAction();

            switch (action.getActionType()) {

                case PONG:



                    if(action.getPongMessage().getActionId().equals(HEART_BEAT_ACTION_ID)){
                        log.debug("Got a heartbeat pong response");
                        ctx.fireChannelReadComplete();
                        return;
                    }

                    log.debug("Got a pong message");

                    ChannelDecorator decorator = new ChannelDecorator(ctx.channel());

                    manager.deliverMessage(msg,decorator.getHost());

                    ctx.fireChannelReadComplete();
                    break;

                default:
                    ctx.fireChannelRead(msg);
                    break;

            }

        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }


    }

    public PongConsumerManager getManager() {
        return manager;
    }

    public void setManager(PongConsumerManager manager) {
        this.manager = manager;
    }
}
