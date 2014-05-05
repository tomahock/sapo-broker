package pt.com.broker.client.nio.bootstrap;

import io.netty.bootstrap.*;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import pt.com.broker.client.nio.HostInfo;
import pt.com.broker.types.NetProtocolType;

/**
 * Created by luissantos on 05-05-2014.
 */
public class BaseBootstrap {


    io.netty.bootstrap.Bootstrap bootstrap;

    private NetProtocolType protocolType;

    protected boolean oldFraming = false;

    public io.netty.bootstrap.Bootstrap getBootstrap() {
        return bootstrap;
    }

    public void setBootstrap(io.netty.bootstrap.Bootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    public NetProtocolType getProtocolType() {
        return protocolType;
    }

    public void setProtocolType(NetProtocolType protocolType) {
        this.protocolType = protocolType;
    }

    public boolean isOldFraming() {
        return oldFraming;
    }

    public void setOldFraming(boolean oldFraming) {
        this.oldFraming = oldFraming;
    }

    public ChannelFuture connect(HostInfo hostInfo){

        io.netty.bootstrap.Bootstrap boot = getBootstrap().clone();

        boot.option(ChannelOption.CONNECT_TIMEOUT_MILLIS,hostInfo.getConnectTimeout());

        ChannelFuture f = boot.connect(hostInfo.getSocketAddress());



        hostInfo.setChannelFuture(f);




        return f;
    }
}
