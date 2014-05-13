package pt.com.broker.client.nio;

import io.netty.channel.ChannelFuture;
import pt.com.broker.client.nio.bootstrap.DatagramBootstrap;
import pt.com.broker.client.nio.bootstrap.DatagramChannelInitializer;
import pt.com.broker.client.nio.server.HostContainer;
import pt.com.broker.types.*;

import java.security.InvalidParameterException;

/**
 * Created by luissantos on 05-05-2014.
 */
public class UdpBrokerClient extends BaseClient {

    public UdpBrokerClient(NetProtocolType ptype) {

        super(ptype);
    }

    public UdpBrokerClient(String host, int port) {
        super(host, port);
    }

    public UdpBrokerClient(String host, int port, NetProtocolType ptype) {
        super(host, port, ptype);

    }

    public UdpBrokerClient(HostInfo host, NetProtocolType ptype) {
        super(host, ptype);
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
    protected void init(NetProtocolType ptype) {
        setBootstrap(new DatagramBootstrap(new DatagramChannelInitializer(ptype)));

        setHosts(new HostContainer(bootstrap));
    }
}
