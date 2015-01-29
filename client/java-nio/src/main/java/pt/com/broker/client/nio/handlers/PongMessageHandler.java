package pt.com.broker.client.nio.handlers;

import java.util.concurrent.atomic.AtomicInteger;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.client.nio.codecs.HeartBeatEventHandler;
import pt.com.broker.client.nio.consumer.PongConsumerManager;
import pt.com.broker.types.ActionIdDecorator;
import pt.com.broker.client.nio.utils.ChannelDecorator;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetMessage;


/**
 * Created by luissantos on 22-04-2014.
 *
 * @author vagrant
 * @version $Id: $Id
 */
@ChannelHandler.Sharable
public class PongMessageHandler extends SimpleChannelInboundHandler<NetMessage> {

    /** Constant <code>HEART_BEAT_ACTION_ID="24bb963d-6d6c-441e-ab4d-999d73578452"</code> */
    public static final String HEART_BEAT_ACTION_ID = "24bb963d-6d6c-441e-ab4d-999d73578452";

    private static final Logger log = LoggerFactory.getLogger(PongMessageHandler.class);

    PongConsumerManager manager;

    /**
     * <p>Constructor for PongMessageHandler.</p>
     *
     * @param manager a {@link pt.com.broker.client.nio.consumer.PongConsumerManager} object.
     */
    public PongMessageHandler(PongConsumerManager manager) {
        super();

        this.manager = manager;
    }

    /** {@inheritDoc} */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }

    /** {@inheritDoc} */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, NetMessage msg){

        NetAction action = msg.getAction();

        if(action.getActionType() != NetAction.ActionType.PONG || action.getPongMessage()  == null){
            ctx.fireChannelRead(msg);
            return;
        }




        try {

            if(getActionId(msg).equals(HeartBeatEventHandler.HEART_BEAT_ACTION_ID)){
            	//Heart beat message. Reset the statistics
            	AtomicInteger heartBeatCounter = ctx.channel().attr(HeartBeatEventHandler.ATTRIBUTE_HEART_BEAT_COUNTER).get();
            	if(heartBeatCounter != null){
            		log.debug("HeartBeat counter: {}", heartBeatCounter.get());
            	}
            	ctx.channel().attr(HeartBeatEventHandler.ATTRIBUTE_HEART_BEAT_COUNTER).remove();
                log.debug("Got a heartbeat pong response");
                return;
            }

            log.debug("Got a pong message");

            ChannelDecorator decorator = new ChannelDecorator(ctx.channel());

            manager.deliverMessage( msg, decorator.getHost() );




        } catch (Throwable throwable) {

            log.error("Was not possible to deliver pong message", throwable);

        }finally {
            ctx.fireChannelReadComplete();
        }


    }


    /**
     * <p>Getter for the field <code>manager</code>.</p>
     *
     * @return a {@link pt.com.broker.client.nio.consumer.PongConsumerManager} object.
     */
    public PongConsumerManager getManager() {
        return manager;
    }

    /**
     * <p>Setter for the field <code>manager</code>.</p>
     *
     * @param manager a {@link pt.com.broker.client.nio.consumer.PongConsumerManager} object.
     */
    public void setManager(PongConsumerManager manager) {
        this.manager = manager;
    }


    private static String getActionId(NetMessage netMessage){

        ActionIdDecorator decorator = new ActionIdDecorator(netMessage);

        return  decorator.getActionId();

    }
}
