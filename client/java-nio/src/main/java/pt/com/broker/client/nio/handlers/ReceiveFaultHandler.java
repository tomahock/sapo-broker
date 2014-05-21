package pt.com.broker.client.nio.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import pt.com.broker.client.nio.consumer.ConsumerManager;
import pt.com.broker.client.nio.events.BrokerListener;
import pt.com.broker.client.nio.events.BrokerListenerAdapter;
import pt.com.broker.types.NetFault;
import pt.com.broker.types.NetMessage;

/**
 * Created by luissantos on 07-05-2014.
 */
public class ReceiveFaultHandler extends SimpleChannelInboundHandler<NetMessage> {


    ConsumerManager manager;

    BrokerListener faultListenerAdapter = null;

    public ReceiveFaultHandler(ConsumerManager manager) {
        super();

       setManager(manager);
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

        //getManager().deliverMessage(msg,ctx.channel());

        if(getFaultListenerAdapter()!=null){
            getFaultListenerAdapter().deliverMessage(msg,ctx.channel());
        }

    }


    public BrokerListener getFaultListenerAdapter() {
        return faultListenerAdapter;
    }

    public void setFaultListenerAdapter(BrokerListener faultListenerAdapter) {
        this.faultListenerAdapter = faultListenerAdapter;
    }
}
