package pt.com.broker.client.nio.utils;

import java.io.IOException;
import java.net.SocketAddress;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import pt.com.broker.client.nio.HostInfo;
import pt.com.broker.client.nio.server.HostContainer;

/**
 * Created by luissantos on 30-05-2014.
 */
public class ChannelDecorator implements Channel {

    public static final AttributeKey<HostInfo> ATTRIBUTE_HOST_INFO  = AttributeKey.valueOf("HOST-INFO");

    protected  Channel instance;

    public ChannelDecorator(Channel instance) {
        this.instance = instance;
    }


    public HostInfo getHost(){
        return getInstance().attr(ATTRIBUTE_HOST_INFO).get();
    }

    public void setHost(HostInfo host){
        getInstance().attr(ATTRIBUTE_HOST_INFO).set(host);
    }

    public Channel getInstance() {
        return instance;
    }










    @Override
    public EventLoop eventLoop() {
        return instance.eventLoop();
    }

    @Override
    public Channel parent() {
        return instance.parent();
    }

    @Override
    public ChannelConfig config() {
        return instance.config();
    }

    @Override
    public boolean isOpen() {
        return instance.isOpen();
    }

    @Override
    public boolean isRegistered() {
        return instance.isRegistered();
    }

    @Override
    public boolean isActive() {
        return instance.isActive();
    }

    @Override
    public ChannelMetadata metadata() {
        return instance.metadata();
    }

    @Override
    public SocketAddress localAddress() {
        return instance.localAddress();
    }

    @Override
    public SocketAddress remoteAddress() {
        return instance.remoteAddress();
    }

    @Override
    public ChannelFuture closeFuture() {
        return instance.closeFuture();
    }

    @Override
    public boolean isWritable() {
        return instance.isWritable();
    }

    @Override
    public Unsafe unsafe() {
        return instance.unsafe();
    }

    @Override
    public ChannelPipeline pipeline() {
        return instance.pipeline();
    }

    @Override
    public ByteBufAllocator alloc() {
        return instance.alloc();
    }

    @Override
    public ChannelPromise newPromise() {
        return instance.newPromise();
    }

    @Override
    public ChannelProgressivePromise newProgressivePromise() {
        return instance.newProgressivePromise();
    }

    @Override
    public ChannelFuture newSucceededFuture() {
        return instance.newSucceededFuture();
    }

    @Override
    public ChannelFuture newFailedFuture(Throwable cause) {
        return instance.newFailedFuture(cause);
    }

    @Override
    public ChannelPromise voidPromise() {
        return instance.voidPromise();
    }

    @Override
    public ChannelFuture bind(SocketAddress localAddress) {
        return instance.bind(localAddress);
    }

    @Override
    public ChannelFuture connect(SocketAddress remoteAddress) {
        return instance.connect(remoteAddress);
    }

    @Override
    public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress) {
        return instance.connect(remoteAddress, localAddress);
    }

    @Override
    public ChannelFuture disconnect() {
        return instance.disconnect();
    }

    @Override
    public ChannelFuture close() {
        return instance.close();
    }

    @Override
    @Deprecated
    public ChannelFuture deregister() {
        return instance.deregister();
    }

    @Override
    public ChannelFuture bind(SocketAddress localAddress, ChannelPromise promise) {
        return instance.bind(localAddress, promise);
    }

    @Override
    public ChannelFuture connect(SocketAddress remoteAddress, ChannelPromise promise) {
        return instance.connect(remoteAddress, promise);
    }

    @Override
    public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
        return instance.connect(remoteAddress, localAddress, promise);
    }

    @Override
    public ChannelFuture disconnect(ChannelPromise promise) {
        return instance.disconnect(promise);
    }

    @Override
    public ChannelFuture close(ChannelPromise promise) {
        return instance.close(promise);
    }

    @Override
    @Deprecated
    public ChannelFuture deregister(ChannelPromise promise) {
        return instance.deregister(promise);
    }

    @Override
    public Channel read() {
        return instance.read();
    }

    @Override
    public ChannelFuture write(Object msg) {
        return instance.write(msg);
    }

    @Override
    public ChannelFuture write(Object msg, ChannelPromise promise) {
        return instance.write(msg, promise);
    }

    @Override
    public Channel flush() {
        return instance.flush();
    }

    @Override
    public ChannelFuture writeAndFlush(Object msg, ChannelPromise promise) {
        return instance.writeAndFlush(msg, promise);
    }

    @Override
    public ChannelFuture writeAndFlush(Object msg) {
        return instance.writeAndFlush(msg);
    }

    @Override
    public <T> Attribute<T> attr(AttributeKey<T> key) {
        return instance.attr(key);
    }

    @Override
    public int compareTo(Channel channel) {
        return instance.compareTo(channel);
    }
}
