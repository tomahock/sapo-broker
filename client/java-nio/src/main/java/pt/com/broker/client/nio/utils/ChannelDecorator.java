package pt.com.broker.client.nio.utils;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import pt.com.broker.client.nio.server.HostInfo;
import pt.com.broker.types.DecoratorInterface;

import java.net.SocketAddress;

/**
 * Created by luissantos on 30-05-2014.
 *
 * @author vagrant
 * @version $Id: $Id
 */
public class ChannelDecorator implements Channel , DecoratorInterface<Channel> {

    /** Constant <code>ATTRIBUTE_HOST_INFO</code> */
    public static final AttributeKey<HostInfo> ATTRIBUTE_HOST_INFO  = AttributeKey.valueOf("HOST-INFO");

    protected  Channel instance;

    /**
     * <p>Constructor for ChannelDecorator.</p>
     *
     * @param instance a {@link io.netty.channel.Channel} object.
     */
    public ChannelDecorator(Channel instance) {
        this.instance = instance;
    }


    /**
     * <p>getHost.</p>
     *
     * @return a {@link pt.com.broker.client.nio.server.HostInfo} object.
     */
    public HostInfo getHost(){
        return getInstance().attr(ATTRIBUTE_HOST_INFO).get();
    }

    /**
     * <p>setHost.</p>
     *
     * @param host a {@link pt.com.broker.client.nio.server.HostInfo} object.
     */
    public void setHost(HostInfo host){
        getInstance().attr(ATTRIBUTE_HOST_INFO).set(host);
    }

    /**
     * <p>Getter for the field <code>instance</code>.</p>
     *
     * @return a {@link io.netty.channel.Channel} object.
     */
    public Channel getInstance() {
        return instance;
    }










    /** {@inheritDoc} */
    @Override
    public EventLoop eventLoop() {
        return instance.eventLoop();
    }

    /** {@inheritDoc} */
    @Override
    public Channel parent() {
        return instance.parent();
    }

    /** {@inheritDoc} */
    @Override
    public ChannelConfig config() {
        return instance.config();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isOpen() {
        return instance.isOpen();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isRegistered() {
        return instance.isRegistered();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isActive() {
        return instance.isActive();
    }

    /** {@inheritDoc} */
    @Override
    public ChannelMetadata metadata() {
        return instance.metadata();
    }

    /** {@inheritDoc} */
    @Override
    public SocketAddress localAddress() {
        return instance.localAddress();
    }

    /** {@inheritDoc} */
    @Override
    public SocketAddress remoteAddress() {
        return instance.remoteAddress();
    }

    /** {@inheritDoc} */
    @Override
    public ChannelFuture closeFuture() {
        return instance.closeFuture();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isWritable() {
        return instance.isWritable();
    }

    /** {@inheritDoc} */
    @Override
    public Unsafe unsafe() {
        return instance.unsafe();
    }

    /** {@inheritDoc} */
    @Override
    public ChannelPipeline pipeline() {
        return instance.pipeline();
    }

    /** {@inheritDoc} */
    @Override
    public ByteBufAllocator alloc() {
        return instance.alloc();
    }

    /** {@inheritDoc} */
    @Override
    public ChannelPromise newPromise() {
        return instance.newPromise();
    }

    /** {@inheritDoc} */
    @Override
    public ChannelProgressivePromise newProgressivePromise() {
        return instance.newProgressivePromise();
    }

    /** {@inheritDoc} */
    @Override
    public ChannelFuture newSucceededFuture() {
        return instance.newSucceededFuture();
    }

    /** {@inheritDoc} */
    @Override
    public ChannelFuture newFailedFuture(Throwable cause) {
        return instance.newFailedFuture(cause);
    }

    /** {@inheritDoc} */
    @Override
    public ChannelPromise voidPromise() {
        return instance.voidPromise();
    }

    /** {@inheritDoc} */
    @Override
    public ChannelFuture bind(SocketAddress localAddress) {
        return instance.bind(localAddress);
    }

    /** {@inheritDoc} */
    @Override
    public ChannelFuture connect(SocketAddress remoteAddress) {
        return instance.connect(remoteAddress);
    }

    /** {@inheritDoc} */
    @Override
    public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress) {
        return instance.connect(remoteAddress, localAddress);
    }

    /** {@inheritDoc} */
    @Override
    public ChannelFuture disconnect() {
        return instance.disconnect();
    }

    /** {@inheritDoc} */
    @Override
    public ChannelFuture close() {
        return instance.close();
    }

    /** {@inheritDoc} */
    @Override
    @Deprecated
    public ChannelFuture deregister() {
        return instance.deregister();
    }

    /** {@inheritDoc} */
    @Override
    public ChannelFuture bind(SocketAddress localAddress, ChannelPromise promise) {
        return instance.bind(localAddress, promise);
    }

    /** {@inheritDoc} */
    @Override
    public ChannelFuture connect(SocketAddress remoteAddress, ChannelPromise promise) {
        return instance.connect(remoteAddress, promise);
    }

    /** {@inheritDoc} */
    @Override
    public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
        return instance.connect(remoteAddress, localAddress, promise);
    }

    /** {@inheritDoc} */
    @Override
    public ChannelFuture disconnect(ChannelPromise promise) {
        return instance.disconnect(promise);
    }

    /** {@inheritDoc} */
    @Override
    public ChannelFuture close(ChannelPromise promise) {
        return instance.close(promise);
    }

    /** {@inheritDoc} */
    @Override
    @Deprecated
    public ChannelFuture deregister(ChannelPromise promise) {
        return instance.deregister(promise);
    }

    /** {@inheritDoc} */
    @Override
    public Channel read() {
        return instance.read();
    }

    /** {@inheritDoc} */
    @Override
    public ChannelFuture write(Object msg) {
        return instance.write(msg);
    }

    /** {@inheritDoc} */
    @Override
    public ChannelFuture write(Object msg, ChannelPromise promise) {
        return instance.write(msg, promise);
    }

    /** {@inheritDoc} */
    @Override
    public Channel flush() {
        return instance.flush();
    }

    /** {@inheritDoc} */
    @Override
    public ChannelFuture writeAndFlush(Object msg, ChannelPromise promise) {
        return instance.writeAndFlush(msg, promise);
    }

    /** {@inheritDoc} */
    @Override
    public ChannelFuture writeAndFlush(Object msg) {
        return instance.writeAndFlush(msg);
    }

    /** {@inheritDoc} */
    @Override
    public <T> Attribute<T> attr(AttributeKey<T> key) {
        return instance.attr(key);
    }

    /** {@inheritDoc} */
    @Override
    public int compareTo(Channel channel) {
        return instance.compareTo(channel);
    }


}
