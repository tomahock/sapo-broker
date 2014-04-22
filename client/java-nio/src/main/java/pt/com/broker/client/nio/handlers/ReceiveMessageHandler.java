package pt.com.broker.client.nio.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import pt.com.broker.client.nio.consumer.ConsumerManager;
import pt.com.broker.types.NetMessage;

/**
 * Created by luissantos on 22-04-2014.
 */
public class ReceiveMessageHandler extends SimpleChannelInboundHandler<NetMessage> {

    ConsumerManager manager;

    public ReceiveMessageHandler(ConsumerManager manager) {
        super();

        this.manager = manager;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, NetMessage msg){


        try {

            manager.deliverMessage(msg);

            System.out.println("Message Received");

        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }


    }

    public ConsumerManager getManager() {
        return manager;
    }
}
