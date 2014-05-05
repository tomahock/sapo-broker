package pt.com.broker.client.nio;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslHandler;
import pt.com.broker.types.NetProtocolType;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Created by luissantos on 05-05-2014.
 */
public class SslBrokerClient extends BrokerClient  {

    protected SSLContext context;

    public SslBrokerClient(NetProtocolType ptype) {
        super(ptype);
    }




    public SslBrokerClient(String host, int port) {
        super(host, port);
    }

    public SslBrokerClient(String host, int port, NetProtocolType ptype) {
        super(host, port, ptype);
    }

    public SslBrokerClient(HostInfo host, NetProtocolType ptype) {
        super(host, ptype);
    }
}
