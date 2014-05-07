package pt.com.broker.client.nio.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import pt.com.broker.client.nio.consumer.ConsumerManager;
import pt.com.broker.types.NetFault;
import pt.com.broker.types.NetMessage;

/**
 * Created by luissantos on 07-05-2014.
 */
public class ReceiveFaultHandler extends SimpleChannelInboundHandler<NetMessage> {


    ConsumerManager manager;

    public ReceiveFaultHandler(ConsumerManager manager) {
        super();

        this.manager = manager;
    }

    public ConsumerManager getManager() {
        return manager;
    }

    public void setManager(ConsumerManager manager) {
        this.manager = manager;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, NetMessage msg) throws Exception {


            if(msg.getAction().getFaultMessage() == null){
                ctx.fireChannelRead(msg);
                return;
            }

            NetFault fault = msg.getAction().getFaultMessage();

            String faultCode = fault.getCode();

            try {

                deliverFaultMessage(ctx,msg);

            } catch (Throwable throwable) {

                 throw new Exception(throwable);
            }


    }

    protected void deliverFaultMessage(ChannelHandlerContext ctx, NetMessage msg) throws Throwable {

        getManager().deliverMessage(msg,ctx.channel());


    }
}
