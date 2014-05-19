package pt.com.broker.client.nio;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

import java.net.InetSocketAddress;

/**
 * Immutable class that represents an Agent host.
 */

public final class HostInfo
{
    public static enum STATUS {
        OPEN, CONNECTING, CLOSED
    }

    public static final int DEFAULT_CONNECT_TIMEOUT = 15 * 1000; // 15 seconds
    public static final int DEFAULT_READ_TIMEOUT = 0; // forever
    public static final int DEFAULT_RECONNECT_LIMIT = 10; // 10 tentativas

    private String hostname;
    private int port;

    private int connectTimeout;
    private final int readTimeout;

    private int reconnectLimit = DEFAULT_RECONNECT_LIMIT;

    private ChannelFuture channelFuture;

    private Channel channel;

    private STATUS status = STATUS.CLOSED;

    /**
     * Creates a HostInfo instance.
     *
     * @param hostname
     *            The name of the host (e.g. broker.localdomain.company.com or 10.12.10.120).
     * @param port
     *            Connection port.
     */
    public HostInfo(String hostname, int port)
    {
        this(hostname, port, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT);
    }



    /**
     * Creates a HostInfo instance.
     *
     * @param hostname
     *            The name of the host (e.g. broker.localdomain.company.com or 10.12.10.120).
     * @param port
     *            Connection port.
     * @param connectTimeout
     *            Connection Timeout
     * @param readTimeout
     *            Read Timeout
     */
    public HostInfo(String hostname, int port, int connectTimeout, int readTimeout)
    {
        this.hostname = hostname;
        this.port = port;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
    }

    public String getHostname()
    {
        return hostname;
    }

    public int getPort()
    {
        return port;
    }

    public synchronized int getConnectTimeout()
    {
        return connectTimeout;
    }

    public synchronized void setConnectTimeout(int connectTimeout)
    {
        this.connectTimeout = connectTimeout;
    }

    public int getReadTimeout()
    {
        return readTimeout;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!obj.getClass().equals(this.getClass())){
            return false;
        }

        HostInfo other = (HostInfo) obj;

        return   com.google.common.base.Objects.equal(this.hostname, other.getHostname())
                && com.google.common.base.Objects.equal(this.port, other.getPort());
    }

    @Override
    public String toString()
    {
        return com.google.common.base.Objects.toStringHelper(this)
                .addValue(this.hostname)
                .addValue(this.port)
                .addValue(this.connectTimeout)
                .addValue(this.readTimeout)
                .addValue(this.status)
                .toString();

    }


    public InetSocketAddress getSocketAddress(){
        InetSocketAddress socketAddress = new InetSocketAddress(getHostname(),getPort());

        return socketAddress;
    }


    public boolean isActive(){

        return this.getChannel() != null && (getChannel().isActive() ||  getChannel().isOpen() );
    }

    public Channel getChannel(){
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public synchronized int getReconnectLimit() {
        return reconnectLimit;
    }

    public  synchronized void setReconnectLimit(int reconnectLimit) {
        this.reconnectLimit = reconnectLimit;
    }

    public  synchronized void resetReconnectLimit(){
        setReconnectLimit(DEFAULT_RECONNECT_LIMIT);
    }

    public synchronized void reconnectAttempt(){
          reconnectLimit--;
    }


    public synchronized STATUS getStatus() {
        return status;
    }

    public synchronized void setStatus(STATUS status) {
        this.status = status;
    }
}