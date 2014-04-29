package pt.com.broker.client.nio.handlers;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import pt.com.broker.client.nio.consumer.ConsumerManager;
import pt.com.broker.types.NetBrokerMessage;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetPublish;

import java.net.InetSocketAddress;


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

        if(ctx==null){
            return;
        }

        try {
            Channel channel = ctx.channel();

            if(msg.getAction().getNotificationMessage() != null){

                NetBrokerMessage bmsg = msg.getAction().getNotificationMessage().getMessage();

                String oldmsgid = bmsg.getMessageId();

                InetSocketAddress socketAddress =  (InetSocketAddress)channel.remoteAddress();

                String newmsgid = socketAddress.getAddress().getCanonicalHostName()+":"+socketAddress.getPort()+"#"+oldmsgid;

                bmsg.setMessageId(newmsgid);

            }

            manager.deliverMessage(msg,channel);

            System.out.println("Message Received");

        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }


    }

    public ConsumerManager getManager() {
        return manager;
    }


}
