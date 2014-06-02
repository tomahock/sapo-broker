package pt.com.broker.client.nio.handlers;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import pt.com.broker.client.nio.consumer.ConsumerManager;
import pt.com.broker.client.nio.events.BrokerListener;
import pt.com.broker.client.nio.utils.ChannelDecorator;
import pt.com.broker.types.NetFault;
import pt.com.broker.types.NetMessage;

/**
 * Created by luissantos on 07-05-2014.
 */
@ChannelHandler.Sharable
public class ReceiveFaultHandler extends SimpleChannelInboundHandler<NetMessage> {


    private ConsumerManager manager;

    private BrokerListener faultListenerAdapter;

    public ReceiveFaultHandler(ConsumerManager manager) {
        super();
        this.manager = manager;
    }

    public ConsumerManager getManager() {
        return manager;
    }




    @Override
    protected void channelRead0(ChannelHandlerContext ctx, NetMessage msg) throws Exception {

        NetFault fault = msg.getAction().getFaultMessage();

        if(fault == null){
            ctx.fireChannelRead(msg);
            return;
        }

        try {

            deliverFaultMessage(ctx,msg);


        } catch (Throwable throwable) {

            throw new Exception(throwable);
        }


    }

    protected void deliverFaultMessage(ChannelHandlerContext ctx, NetMessage msg) throws Throwable {

        NetFault fault = msg.getAction().getFaultMessage();

        ChannelDecorator decorator = new ChannelDecorator(ctx.channel());

        if(fault.getCode().equals(NetFault.PollTimeoutErrorCode)){



            getManager().deliverMessage(msg,decorator);
            return;
        }


        BrokerListener listener = getFaultListenerAdapter();

        if(listener!=null){
            getFaultListenerAdapter().deliverMessage(msg,decorator);
        }

    }

    public BrokerListener getFaultListenerAdapter() {
        return faultListenerAdapter;
    }

    public void setFaultListenerAdapter(BrokerListener faultListenerAdapter) {
        this.faultListenerAdapter = faultListenerAdapter;
    }


}
