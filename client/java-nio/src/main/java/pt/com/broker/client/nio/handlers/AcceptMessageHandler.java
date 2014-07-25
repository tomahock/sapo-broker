package pt.com.broker.client.nio.handlers;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.com.broker.client.nio.consumer.PendingAcceptRequestsManager;
import pt.com.broker.client.nio.utils.ChannelDecorator;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetMessage;

/**
 * Created by luissantos on 09-05-2014.
 *
 * @author vagrant
 * @version $Id: $Id
 */
@ChannelHandler.Sharable()
public class AcceptMessageHandler extends SimpleChannelInboundHandler<NetMessage> {


    private static final Logger log = LoggerFactory.getLogger(AcceptMessageHandler.class);

    PendingAcceptRequestsManager manager;


    /**
     * <p>Constructor for AcceptMessageHandler.</p>
     *
     * @param manager a {@link pt.com.broker.client.nio.consumer.PendingAcceptRequestsManager} object.
     */
    public AcceptMessageHandler(PendingAcceptRequestsManager manager) {
        this.manager = manager;
    }

    /** {@inheritDoc} */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, NetMessage msg) throws Exception {

        NetAction action = msg.getAction();

        if(action.getActionType() != NetAction.ActionType.ACCEPTED && action.getActionType() != NetAction.ActionType.FAULT ) {
            ctx.fireChannelRead(msg);
            return;
        }

        if(action.getActionType() == NetAction.ActionType.FAULT){

            String actionId = action.getFaultMessage().getActionId();

            log.debug("Got Fault Message.  ActionId: {}",actionId);

            if(manager.getListener(actionId)==null){

                ctx.fireChannelRead(msg);
                return;

            }
        }


        ChannelDecorator decorator = new ChannelDecorator(ctx.channel());
        manager.deliverMessage(msg,decorator.getHost());

    }

    /**
     * <p>Getter for the field <code>manager</code>.</p>
     *
     * @return a {@link pt.com.broker.client.nio.consumer.PendingAcceptRequestsManager} object.
     */
    public PendingAcceptRequestsManager getManager() {
        return manager;
    }

    /**
     * <p>Setter for the field <code>manager</code>.</p>
     *
     * @param manager a {@link pt.com.broker.client.nio.consumer.PendingAcceptRequestsManager} object.
     */
    public void setManager(PendingAcceptRequestsManager manager) {
        this.manager = manager;
    }
}
