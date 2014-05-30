package pt.com.broker.client.nio;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.com.broker.auth.AuthInfo;
import pt.com.broker.auth.CredentialsProvider;
import pt.com.broker.client.nio.bootstrap.ChannelInitializer;
import pt.com.broker.client.nio.events.AcceptResponseListener;
import pt.com.broker.client.nio.server.HostInfo;
import pt.com.broker.types.*;

import javax.net.ssl.*;
import java.security.KeyStore;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;

/**
 * Created by luissantos on 05-05-2014.
 */
public class SslBrokerClient extends BrokerClient  {

    private static final Logger log = LoggerFactory.getLogger(SslBrokerClient.class);

    protected SSLContext context;

    protected AuthInfo userCredentials;

    private CredentialsProvider credentialsProvider;



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
    public Future<HostInfo> connectAsync() {

        if(getContext()==null){
            setContext(getDefaultSslContext());
        }

        return super.connectAsync();
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

    public void setCredentialsProvider(CredentialsProvider credentialsProvider)
    {
        this.credentialsProvider = credentialsProvider;
    }

    public synchronized boolean authenticateClient() throws Throwable
    {
        if (this.credentialsProvider == null)
        {
            throw new IllegalStateException("Mandatory Credential Provider missing.");
        }


        //setState(BrokerClientState.AUTH);

        this.userCredentials = credentialsProvider.getCredentials();

        NetAuthentication clientAuth = new NetAuthentication(userCredentials.getToken(), userCredentials.getUserAuthenticationType());
        clientAuth.setRoles(userCredentials.getRoles());
        

        if (userCredentials.getUserId() != null)
            clientAuth.setUserId(userCredentials.getUserId());

        final BlockingQueue<Boolean> queue = new ArrayBlockingQueue<Boolean>(1);


       AcceptResponseListener acceptedListener = new AcceptResponseListener(){

            @Override
            public void onMessage(NetAccepted message, HostInfo host) {
                queue.add(true);
            }

            @Override
            public void onFault(NetFault fault, HostInfo host) {

                log.error(String.format("Authentication failed: %s", fault.getMessage()));

                queue.add(false);
            }

            @Override
            public void onTimeout(String actionID) {

                log.warn("Authentication failed by timeout.");

                queue.add(false);
            }
        };


        sendAuthMessage(clientAuth,acceptedListener,10000);



        return queue.take().booleanValue();

    }

    protected Future<Void> sendAuthMessage(NetAuthentication authentication, AcceptResponseListener acceptResponseListener , long timeout ){


        if(acceptResponseListener!=null) {

            String actionId = UUID.randomUUID().toString();

            authentication.setActionId(actionId);

            AcceptRequest acceptRequest = new AcceptRequest(actionId, acceptResponseListener, timeout);

            addAcceptMessageHandler(acceptRequest);
        }

        NetMessage msg = new NetMessage(new NetAction(authentication));

        return sendNetMessage(msg);
    }



}
