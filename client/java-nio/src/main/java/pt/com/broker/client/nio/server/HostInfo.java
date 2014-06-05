package pt.com.broker.client.nio.server;

import io.netty.channel.Channel;


/**
 * Immutable class that represents an Agent host.
 */

public final class HostInfo
{
    public static enum STATUS {
        OPEN, CONNECTING, CLOSED, DISABLE
    }

    public static final int DEFAULT_CONNECT_TIMEOUT = 15 * 1000; // 15 seconds
    public static final int DEFAULT_READ_TIMEOUT = 0; // forever
    public static final int DEFAULT_RECONNECT_LIMIT = 10; // 10 tentativas

    private String hostname;
    private int port;

    private int connectTimeout;
    private int reconnectLimit = DEFAULT_RECONNECT_LIMIT;

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
        this(hostname, port, DEFAULT_CONNECT_TIMEOUT);
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
     */
    public HostInfo(String hostname, int port, int connectTimeout)
    {
        this.hostname = hostname;
        this.port = port;
        this.connectTimeout = connectTimeout;
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


    @Override
    public boolean equals(Object obj)
    {
        if (!obj.getClass().equals(this.getClass()))
        return false;
        HostInfo other = (HostInfo) obj;
        if (!hostname.equals(other.hostname))
            return false;
        if (port != other.port)
            return false;

        return true;
    }

    @Override
    public String toString()
    {
        return String.format("HostInfo [hostname=%s, port=%s]", hostname, port);
    }


    public boolean isActive(){
        return this.getChannel() != null && (getChannel().isActive() &&  getChannel().isOpen() && getChannel().isWritable() );
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public synchronized int getReconnectLimit() {
        return reconnectLimit;
    }

    public synchronized void setReconnectLimit(int reconnectLimit) {
        this.reconnectLimit = reconnectLimit;
    }

    protected synchronized void resetReconnectLimit(){
        setReconnectLimit(DEFAULT_RECONNECT_LIMIT);
    }

    protected synchronized void reconnectAttempt(){
        if(reconnectLimit-- <= 0){
            setStatus(STATUS.DISABLE);
        }
    }


    public synchronized STATUS getStatus() {
        return status;
    }

    public synchronized void setStatus(STATUS status) {
        this.status = status;
    }
}