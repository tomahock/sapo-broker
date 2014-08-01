package pt.com.broker.ws.models;

import io.netty.channel.Channel;
import org.codehaus.jackson.annotate.JsonProperty;

import java.net.InetSocketAddress;

/**
 * Copyright (c) 2014, SAPO
 * All rights reserved.
 *
 * <p/>
 * Created by Luis Santos<luis.santos@telecom.pt> on 25-06-2014.
 */
public class Agent {

    Channel channel;


    public Agent(Channel channel) {
        this.channel = channel;
    }


    @JsonProperty("host")
    public String getHost(){


        return ((InetSocketAddress)channel.remoteAddress()).getHostString();
    }

    @JsonProperty("port")
    public int getPort(){
        return ((InetSocketAddress)channel.remoteAddress()).getPort();
    }
}
