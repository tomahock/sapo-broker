package pt.com.broker.client.nio.mocks;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.com.broker.client.nio.codecs.BindingSerializerFactory;
import pt.com.broker.client.nio.codecs.BrokerMessageDecoder;
import pt.com.broker.client.nio.codecs.BrokerMessageEncoder;
import pt.com.broker.types.*;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.Future;


/**
 * Created by luissantos on 12-05-2014.
 */
public class SocketServer {

    private static final Logger log = LoggerFactory.getLogger(SocketServer.class);

    int port;


    private ChannelFuture future;

    EventLoopGroup bossGroup = new NioEventLoopGroup();
    EventLoopGroup workerGroup = new NioEventLoopGroup();
    ServerBootstrap b = new ServerBootstrap();

    public SocketServer() {
        this(0);
    }
    public SocketServer(int port) {
        this.port = port;
    }

    private class InputHandler extends ChannelInboundHandlerAdapter{

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) { // (2)

            if(msg instanceof NetMessage){

                NetMessage _msg = (NetMessage)msg;

                if(_msg.getAction().getActionType() == NetAction.ActionType.PING) {

                    NetMessage netMessage = new NetMessage(new NetAction(new NetPong(_msg.getAction().getPingMessage().getActionId())));

                    ctx.writeAndFlush(netMessage);
                }

            }

            ctx.fireChannelReadComplete();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)
            // Close the connection when an exception is raised.
            cause.printStackTrace();
            ctx.close();
        }

    }


    protected int bind() throws Exception {

        ChannelFuture future = run();


        setFuture(future);


        InetSocketAddress address = (InetSocketAddress)future.channel().localAddress();



        this.port = address.getPort();

        log.debug("Test server running on port: "+this.getPort());

        return this.getPort();
    }



    private ChannelFuture run() throws Exception {




        try {


            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class) // (3)
                    .childHandler(new ChannelInitializer<SocketChannel>() { // (4)
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {


                            BindingSerializer binding = BindingSerializerFactory.getInstance(NetProtocolType.JSON);


                            ch.pipeline().addLast("broker_message_decoder", new BrokerMessageDecoder(binding));
                            ch.pipeline().addLast("broker_message_encoder", new BrokerMessageEncoder(binding));
                            ch.pipeline().addLast("server_handler", new InputHandler());


                            log.debug("Remote client connected");


                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)          // (5)
                    .childOption(ChannelOption.SO_KEEPALIVE, true); // (6)

            // Bind and start to accept incoming connections.
            ChannelFuture f = b.bind("0.0.0.0",getPort()).sync(); // (7)

            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to gracefully
            // shut down your server.
            //f.channel().closeFuture().sync();



            return f;

        } finally {

            //workerGroup.shutdownGracefully();
            //bossGroup.shutdownGracefully();

        }



    }

    public int getPort() {
        return port;
    }

    private ChannelFuture getFuture() {
        return future;
    }

    private void setFuture(ChannelFuture future) {
        this.future = future;
    }


    public Future shutdown(){

        return b.childGroup().shutdownGracefully();
    }


    public void badShutdown(){


        b.childGroup().shutdown();
    }
}
