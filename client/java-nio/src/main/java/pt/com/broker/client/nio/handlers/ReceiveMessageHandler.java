package pt.com.broker.client.nio.handlers;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.com.broker.client.nio.consumer.ConsumerManager;
import pt.com.broker.client.nio.utils.NetNotificationDecorator;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetBrokerMessage;
import pt.com.broker.types.NetMessage;

import java.net.InetSocketAddress;


/**
 * Created by luissantos on 22-04-2014.
 */
public class ReceiveMessageHandler extends SimpleChannelInboundHandler<NetMessage> {

    private static final Logger log = LoggerFactory.getLogger(ReceiveMessageHandler.class);

    ConsumerManager manager;

    public ReceiveMessageHandler(ConsumerManager manager) {
        super();

        this.manager = manager;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, NetMessage msg){

        if(ctx==null){
            return;
        }

        try {


            NetAction action = msg.getAction();

            switch (action.getActionType()) {

                case NOTIFICATION:

                    // Modifies the NetNotification to identify the channel
                    NetNotificationDecorator decorator = new NetNotificationDecorator(msg.getAction().getNotificationMessage(),ctx.channel());

                    msg.getAction().setNotificationMessage(decorator);

                    this.deliverNotification(ctx, msg);

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

    public ConsumerManager getManager() {
        return manager;
    }

    protected void deliverNotification(ChannelHandlerContext ctx, NetMessage msg) throws Throwable {

        Channel channel = ctx.channel();

        log.debug("Message Received");
        
        manager.deliverMessage(msg,channel);



    }


}
