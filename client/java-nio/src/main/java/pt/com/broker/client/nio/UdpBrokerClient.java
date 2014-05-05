package pt.com.broker.client.nio;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import pt.com.broker.client.nio.bootstrap.DatagramBootstrap;
import pt.com.broker.client.nio.utils.HostContainer;
import pt.com.broker.types.*;

import java.security.InvalidParameterException;
import java.util.concurrent.Future;

/**
 * Created by luissantos on 05-05-2014.
 */
public class UdpBrokerClient extends BaseClient {

    private DatagramBootstrap bootstrap;


    protected ChannelFuture channelFuture;

    public UdpBrokerClient(NetProtocolType ptype) {

        setProtocolType(ptype);

        setBootstrap(new DatagramBootstrap(ptype,false));


        hosts = new HostContainer(getBootstrap());

    }

    public UdpBrokerClient(String host, int port) {

        this(new HostInfo(host, port), NetProtocolType.JSON);


    }

    public UdpBrokerClient(String host, int port, NetProtocolType ptype) {

        this(new HostInfo(host, port), ptype);


    }

    public UdpBrokerClient(HostInfo host, NetProtocolType ptype) {

        this(ptype);

        getHosts().add(host);
    }


    @Override
    public ChannelFuture publishMessage(NetPublish message, String destination, NetAction.DestinationType dtype) {

        if (message.getActionId() != null)
        {
            throw new InvalidParameterException("Messages published over UDP are not allowed to carry a message identifier.");
        }

        return super.publishMessage(message, destination, dtype);
    }



    @Override
    public void setProtocolType(NetProtocolType protocolType) {

        if(! (protocolType == NetProtocolType.PROTOCOL_BUFFER || protocolType == NetProtocolType.THRIFT) ){
            log.warn("Using non-binary encoding with datagram transport will add some overhead ");
        }


        super.setProtocolType(protocolType);
    }


}
