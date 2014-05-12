package pt.com.broker.client.nio.mocks;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.Future;


/**
 * Created by luissantos on 12-05-2014.
 */
public class SocketServer {

    private static final Logger log = LoggerFactory.getLogger(SocketServer.class);

    int port;


    private ChannelFuture future;


    public SocketServer() {
        this(0);
    }
    public SocketServer(int port) {
        this.port = port;
    }

    private class Handler extends ChannelInboundHandlerAdapter{

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) { // (2)
            // Discard the received data silently.
            ((ByteBuf) msg).release(); // (3)
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


        EventLoopGroup bossGroup = new NioEventLoopGroup(); // (1)
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {

            ServerBootstrap b = new ServerBootstrap(); // (2)
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class) // (3)
                    .childHandler(new ChannelInitializer< SocketChannel>() { // (4)
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {

                            ch.pipeline().addLast(new Handler());

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

        try {


            getFuture().channel().close().awaitUninterruptibly();


        } catch (Exception e) {
            e.printStackTrace();
        }

        return getFuture().channel().eventLoop().shutdownGracefully();
    }
}
