package pt.com.broker.client.nio.handlers;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetPing;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;

public class HeartBeatEventHandler extends ChannelDuplexHandler {
	
	private static final Logger log = LoggerFactory.getLogger(HeartBeatEventHandler.class);
	
	/** Constant <code>HEART_BEAT_ACTION_ID="24bb963d-6d6c-441e-ab4d-999d73578452"</code> */
    public static final String HEART_BEAT_ACTION_ID = "24bb963d-6d6c-441e-ab4d-999d73578452";
	public static final int HEART_BEAT_ATTEMPTS = 3;
	public static final AttributeKey<AtomicInteger> ATTRIBUTE_HEART_BEAT_COUNTER = AttributeKey.valueOf("HBCOUNTER");

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
			throws Exception {
		if(evt instanceof IdleStateEvent){
			IdleStateEvent idleEvent = (IdleStateEvent) evt;
			if(idleEvent.state() == IdleState.READER_IDLE){
				//When the reader is idle, we have to check how many consecutive idle events we received.
				AtomicInteger heartBeatCounter = ctx.channel().attr(ATTRIBUTE_HEART_BEAT_COUNTER).get();
				if(heartBeatCounter != null && heartBeatCounter.incrementAndGet() >= HEART_BEAT_ATTEMPTS){
					log.debug("Reader Idle and all heart beat attempts exausted. Closing the connection.");
					//We exausted the attempts for heart beat to work. Close the connection and reset
					//the properties
					ctx.channel().attr(ATTRIBUTE_HEART_BEAT_COUNTER).remove();
					ctx.close();
				} else {
					log.debug("Reader Idle. Sending heartbeat ping message.");
					ctx.channel().attr(ATTRIBUTE_HEART_BEAT_COUNTER).setIfAbsent(new AtomicInteger());
					ctx.channel().attr(ATTRIBUTE_HEART_BEAT_COUNTER).get().incrementAndGet();
					NetMessage message = new NetMessage(new NetAction(new NetPing(HEART_BEAT_ACTION_ID)));
			    	ctx.writeAndFlush(message);
				}
			}
		} else {
			//Nothing to do here, we forward the event to the next handler in the pipeline.
			ctx.fireUserEventTriggered(evt);
		}
	}

}
