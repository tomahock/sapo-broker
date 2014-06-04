package pt.com.broker.client.nio.handlers;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import pt.com.broker.client.nio.consumer.PendingAcceptRequestsManager;
import pt.com.broker.client.nio.types.ActionIdDecorator;
import pt.com.broker.client.nio.utils.ChannelDecorator;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetMessage;

/**
 * Created by luissantos on 09-05-2014.
 */
@ChannelHandler.Sharable()
public class AcceptMessageHandler extends SimpleChannelInboundHandler<NetMessage> {


    PendingAcceptRequestsManager manager;


    public AcceptMessageHandler(PendingAcceptRequestsManager manager) {
        this.manager = manager;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, NetMessage msg) throws Exception {

        NetAction action = msg.getAction();


        if(action.getActionType() != NetAction.ActionType.ACCEPTED || manager == null) {
            ctx.fireChannelRead(msg);
            return;
        }

        ChannelDecorator decorator = new ChannelDecorator(ctx.channel());
        manager.deliverMessage(msg,decorator.getHost());

    }

    public PendingAcceptRequestsManager getManager() {
        return manager;
    }

    public void setManager(PendingAcceptRequestsManager manager) {
        this.manager = manager;
    }
}
