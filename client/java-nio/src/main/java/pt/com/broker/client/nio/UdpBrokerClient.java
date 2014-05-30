package pt.com.broker.client.nio;

import pt.com.broker.client.nio.bootstrap.BaseChannelInitializer;
import pt.com.broker.client.nio.bootstrap.DatagramBootstrap;
import pt.com.broker.client.nio.bootstrap.DatagramChannelInitializer;
import pt.com.broker.client.nio.server.HostContainer;
import pt.com.broker.client.nio.server.HostInfo;
import pt.com.broker.types.*;

import java.security.InvalidParameterException;
import java.util.concurrent.Future;

/**
 * Created by luissantos on 05-05-2014.
 */
public class UdpBrokerClient extends BaseClient {

    public UdpBrokerClient(NetProtocolType ptype) {
        super(ptype);
        connect();
    }

    public UdpBrokerClient(String host, int port) {
        super(host, port);
        connect();
    }

    public UdpBrokerClient(String host, int port, NetProtocolType ptype) {
        super(host, port, ptype);
        connect();
    }

    public UdpBrokerClient(HostInfo host, NetProtocolType ptype) {
        super(host, ptype);
        connect();
    }

    @Override
    public Future publish(NetPublish message, String destination, NetAction.DestinationType dtype) {

        if (message.getActionId() != null)
        {
            throw new InvalidParameterException("Messages published over UDP are not allowed to carry a message identifier.");
        }

        return super.publish(message, destination, dtype);
    }



    @Override
    protected void init() {

        BaseChannelInitializer channelInitializer = new DatagramChannelInitializer(getSerializer());

        channelInitializer.setOldFraming(getProtocolType() == NetProtocolType.SOAP_v0);

        setBootstrap(new DatagramBootstrap(channelInitializer));

        setHosts(new HostContainer(bootstrap));
    }
}
