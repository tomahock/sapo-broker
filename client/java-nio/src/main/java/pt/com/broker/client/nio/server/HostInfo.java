package pt.com.broker.client.nio.server;

import io.netty.channel.Channel;
import io.netty.util.UniqueName;
import io.netty.util.internal.PlatformDependent;

import java.util.concurrent.ConcurrentMap;


/**
 * Immutable class that represents an Agent host.
 *
 * @author vagrant
 * @version $Id: $Id
 */
public final class HostInfo
{
    public static enum STATUS {
        OPEN, CONNECTING, CLOSED, DISABLE
    }

    /** Constant <code>DEFAULT_CONNECT_TIMEOUT=15 * 1000</code> */
    public static final int DEFAULT_CONNECT_TIMEOUT = 15 * 1000; // 15 seconds
    /** Constant <code>DEFAULT_READ_TIMEOUT=0</code> */
    public static final int DEFAULT_READ_TIMEOUT = 0; // forever
    /** Constant <code>DEFAULT_RECONNECT_LIMIT=10</code> */
    public static final int DEFAULT_RECONNECT_LIMIT = 10; // 10 tentativas

    private String hostname;
    private int port;

    private int connectTimeout;
    private int reconnectLimit = DEFAULT_RECONNECT_LIMIT;

    private Channel channel;
    private STATUS status = STATUS.CLOSED;

    private long readerIdleTime = 20000;
    private long writerIdleTime = 40000;




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

    /**
     * <p>Getter for the field <code>hostname</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getHostname()
    {
        return hostname;
    }

    /**
     * <p>Getter for the field <code>port</code>.</p>
     *
     * @return a int.
     */
    public int getPort()
    {
        return port;
    }

    /**
     * <p>Getter for the field <code>connectTimeout</code>.</p>
     *
     * @return a int.
     */
    public synchronized int getConnectTimeout()
    {
        return connectTimeout;
    }

    /**
     * <p>Setter for the field <code>connectTimeout</code>.</p>
     *
     * @param connectTimeout a int.
     */
    public synchronized void setConnectTimeout(int connectTimeout)
    {
        this.connectTimeout = connectTimeout;
    }


    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return String.format("HostInfo [hostname=%s, port=%s]", hostname, port);
    }


    /**
     * <p>isActive.</p>
     *
     * @return a boolean.
     */
    public boolean isActive(){
        return this.getChannel() != null && (getChannel().isActive() &&  getChannel().isOpen() && getChannel().isWritable() );
    }

    /**
     * <p>Getter for the field <code>channel</code>.</p>
     *
     * @return a {@link io.netty.channel.Channel} object.
     */
    public Channel getChannel() {
        return channel;
    }

    /**
     * <p>Setter for the field <code>channel</code>.</p>
     *
     * @param channel a {@link io.netty.channel.Channel} object.
     */
    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    /**
     * <p>Getter for the field <code>reconnectLimit</code>.</p>
     *
     * @return a int.
     */
    public synchronized int getReconnectLimit() {
        return reconnectLimit;
    }

    /**
     * <p>Setter for the field <code>reconnectLimit</code>.</p>
     *
     * @param reconnectLimit a int.
     */
    public synchronized void setReconnectLimit(int reconnectLimit) {
        this.reconnectLimit = reconnectLimit;
    }

    /**
     * <p>resetReconnectLimit.</p>
     */
    protected synchronized void resetReconnectLimit(){
        setReconnectLimit(DEFAULT_RECONNECT_LIMIT);
    }

    /**
     * <p>reconnectAttempt.</p>
     */
    protected synchronized void reconnectAttempt(){

        if(reconnectLimit-- <= 0){
            setStatus(STATUS.DISABLE);
        }

    }


    /**
     * <p>Getter for the field <code>status</code>.</p>
     *
     * @return a {@link pt.com.broker.client.nio.server.HostInfo.STATUS} object.
     */
    public synchronized STATUS getStatus() {
        return status;
    }

    /**
     * <p>Setter for the field <code>status</code>.</p>
     *
     * @param status a {@link pt.com.broker.client.nio.server.HostInfo.STATUS} object.
     */
    public synchronized void setStatus(STATUS status) {
        this.status = status;
    }


    public long getReaderIdleTime() {
        return readerIdleTime;
    }

    public synchronized void setReaderIdleTime(long readerIdleTime) {
        this.readerIdleTime = readerIdleTime;
    }

    public long getWriterIdleTime() {
        return writerIdleTime;
    }

    public synchronized void setWriterIdleTime(long writerIdleTime) {
        this.writerIdleTime = writerIdleTime;
    }
}
