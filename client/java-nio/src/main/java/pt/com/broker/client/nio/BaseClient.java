package pt.com.broker.client.nio;

import com.google.common.util.concurrent.ListenableFuture;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.GenericFutureListener;
import org.caudexorigo.text.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.com.broker.client.nio.bootstrap.BaseBootstrap;
import pt.com.broker.client.nio.codecs.BindingSerializerFactory;
import pt.com.broker.client.nio.server.HostContainer;
import pt.com.broker.types.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Observer;
import java.util.concurrent.Future;

/**
 * Created by luissantos on 05-05-2014.
 */
public abstract class BaseClient{

    private static final Logger log = LoggerFactory.getLogger(BaseClient.class);

    protected HostContainer hosts;

    BaseBootstrap bootstrap;

    BindingSerializer serializer = null;
    NetProtocolType protocolType = NetProtocolType.JSON;


    public BaseClient(NetProtocolType ptype) {

        setProtocolType(ptype);
        init();

    }

    public BaseClient(String host, int port) {

        this(new HostInfo(host, port), NetProtocolType.JSON);


    }

    public BaseClient(String host, int port, NetProtocolType ptype) {

        this(new HostInfo(host, port), ptype);


    }

    public BaseClient(HostInfo host, NetProtocolType ptype) {

        this(ptype);

        this.addServer(host);
    }

    protected ChannelFuture sendNetMessage(NetMessage msg) {
        return this.sendNetMessage(msg,null);
    }

    protected ChannelFuture sendNetMessage(NetMessage msg, GenericFutureListener future) {
        return this.sendNetMessage(msg,future,null);
    }

    protected ChannelFuture sendNetMessage(NetMessage msg, GenericFutureListener future, Channel c) {

        Channel channel = (c == null) ? getChannel() : c;

        ChannelFuture f =  channel.writeAndFlush(msg);

        if(future!=null){
            f.addListener(future);
        }


        return f;
    }

    protected NetMessage buildMessage(NetAction action, Map<String, String> headers) {
        NetMessage message = new NetMessage(action, headers);

        return message;
    }


    protected NetMessage buildMessage(NetAction action) {
        return this.buildMessage(action, new HashMap<String, String>());
    }

    public ChannelFuture publishMessage(String brokerMessage, String destinationName,NetAction.DestinationType dtype) {

        return publishMessage(brokerMessage.getBytes(), destinationName, dtype);
    }

    public ChannelFuture publishMessage(byte[] brokerMessage, String destinationName , NetAction.DestinationType dtype) {

        NetBrokerMessage msg = new NetBrokerMessage(brokerMessage);

        return publishMessage(msg, destinationName, dtype);
    }

    public ChannelFuture publishMessage(NetBrokerMessage brokerMessage, String destination, NetAction.DestinationType dtype) {

        if ((brokerMessage == null) || StringUtils.isBlank(destination)) {
            throw new IllegalArgumentException("Mal-formed Enqueue request");
        }

        NetPublish publish = new NetPublish(destination, dtype, brokerMessage);


        return publishMessage(publish,destination,dtype);

    }

    public ChannelFuture publishMessage(NetPublish message, String destination, NetAction.DestinationType dtype) {

        NetAction action = new NetAction(message);

        return sendNetMessage(new NetMessage(action, message.getMessage().getHeaders()),null);

    }

    protected Channel getChannel() {

        Channel c = null;

        try {

            c = getHosts().getAvailableChannel();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(c==null){
            throw new RuntimeException("Was not possible to get an active channel");
        }

        log.debug("Selected channel is: "+c.toString());

        return c;
    }

    public HostInfo connect(){

        return hosts.connect();

    }

    public Future<HostInfo> connectAsync(){

        return hosts.connectAsync();

    }


    public HostContainer getHosts() {
        return hosts;
    }

    public void setHosts(HostContainer hosts) {
        this.hosts = hosts;
    }

    public BaseBootstrap getBootstrap() {
        return bootstrap;
    }

    public void setBootstrap(BaseBootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    public Future close() {
        return getBootstrap().shutdownGracefully();
    }


    public void addServer(HostInfo host) {

        getHosts().add(host);
    }

    public void addServer(String hostname, int port) {

        this.addServer(new HostInfo(hostname, port));
    }


    protected  abstract void init();


    public NetProtocolType getProtocolType() {
        return protocolType;
    }

    public void setProtocolType(NetProtocolType protocolType) {

        this.protocolType = protocolType;

        try {
            serializer = BindingSerializerFactory.getInstance(protocolType);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    public BindingSerializer getSerializer() {
        return serializer;
    }
}


