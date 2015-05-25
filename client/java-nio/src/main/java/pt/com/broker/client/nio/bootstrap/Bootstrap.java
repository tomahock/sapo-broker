package pt.com.broker.client.nio.bootstrap;


import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

import org.caudexorigo.netty.NettyContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.client.nio.server.HostInfo;

/**
 * Created by luissantos on 23-04-2014.
 *
 * @author vagrant
 * @version $Id: $Id
 */
public class Bootstrap extends BaseBootstrap {
	
	static final Logger log = LoggerFactory.getLogger(Bootstrap.class);
	private Class<? extends Channel> channel;

    /**
     * <p>Constructor for Bootstrap.</p>
     *
     * @param channelInitializer a {@link pt.com.broker.client.nio.bootstrap.BaseChannelInitializer} object.
     */
    public Bootstrap(BaseChannelInitializer channelInitializer, NettyContext nettyCtx) {
        super(channelInitializer, nettyCtx);
    }

    /**
     * <p>getNewInstance.</p>
     *
     * @return a {@link io.netty.bootstrap.Bootstrap} object.
     */
    
    public io.netty.bootstrap.Bootstrap getNewInstance(ByteBufAllocator allocator){

        io.netty.bootstrap.Bootstrap bootstrap = new io.netty.bootstrap.Bootstrap();

        EventLoopGroup group = getGroup();


       bootstrap.group(group).channel(getNettyContext().getChannelClass());
       bootstrap.option(ChannelOption.ALLOCATOR, allocator);

       bootstrap.handler(getChannelInitializer());

        return  bootstrap;
    }



	@Override
    public ChannelFuture connect(final HostInfo hostInfo) {
        ChannelFuture f = super.connect(hostInfo);
        f.addListener(new ChannelFutureListener() {
        	
            @Override
            public void operationComplete(ChannelFuture f) throws Exception {

                if (f.isSuccess()) {
                	log.debug("Adding iddle state handler to the pipeline. Reader idle timeout: {}. Writter idle timeout: {}", hostInfo.getReaderIdleTime(), hostInfo.getWriterIdleTime());
                    IdleStateHandler idleStateHandler = new IdleStateHandler(hostInfo.getReaderIdleTime(), hostInfo.getWriterIdleTime(), 0, TimeUnit.MILLISECONDS);
//                    f.channel().pipeline().addBefore("heartbeat_handler", "idle_state_handler", idleStateHandler);
                    f.channel().pipeline().addFirst(idleStateHandler);

                }
            }
            
        });
        return f;
    }
    
 
}
