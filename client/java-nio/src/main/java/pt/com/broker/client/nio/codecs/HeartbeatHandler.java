package pt.com.broker.client.nio.codecs;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetPing;

import java.util.UUID;

/**
 * Created by luissantos on 15-05-2014.
 */
@ChannelHandler.Sharable()
public class HeartbeatHandler extends ChannelDuplexHandler {

    public static final String HEART_BEAT_ACTION_ID = "24bb963d-6d6c-441e-ab4d-999d73578452";

    private static final Logger log = LoggerFactory.getLogger(HeartbeatHandler.class);

    @Override
    public void  userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {


        if (evt instanceof IdleStateEvent) {

                IdleStateEvent e = (IdleStateEvent) evt;

                if (e.state() == IdleState.READER_IDLE) {


                    //close channel when there is no input messages
                    ctx.close();


                } else if (e.state() == IdleState.WRITER_IDLE) {

                    log.debug("Send ping keep alive message");
                    //send ping message
                    NetMessage message = new NetMessage(new NetAction(new NetPing(HEART_BEAT_ACTION_ID)));

                    ctx.writeAndFlush(message);
                }
        }


    }
}
