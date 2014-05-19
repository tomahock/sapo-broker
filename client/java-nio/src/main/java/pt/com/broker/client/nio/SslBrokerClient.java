package pt.com.broker.client.nio;


import com.google.common.util.concurrent.ListenableFuture;
import pt.com.broker.client.nio.bootstrap.ChannelInitializer;
import pt.com.broker.types.NetProtocolType;

import javax.net.ssl.*;
import java.security.KeyStore;
import java.util.concurrent.Future;

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

    public SSLContext getContext() {
        return context;
    }

    public void setContext(SSLContext context) {

        this.context = context;

        ChannelInitializer channelInitializer = (ChannelInitializer)getBootstrap().getChannelInitializer();

        channelInitializer.setContext(context);
    }

    @Override
    public ListenableFuture<HostInfo> connect() throws Exception {

        if(getContext()==null){
            setContext(getDefaultSslContext());
        }

        return super.connect();
    }


    private SSLContext getDefaultSslContext()
    {

        try
        {

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());

            tmf.init((KeyStore) null);

            SSLContext sc = SSLContext.getInstance("TLS");

            sc.init(null, tmf.getTrustManagers(), new java.security.SecureRandom());


            return sc;
        }
        catch (Throwable t)
        {
            throw new RuntimeException(t);
        }

    }
}
