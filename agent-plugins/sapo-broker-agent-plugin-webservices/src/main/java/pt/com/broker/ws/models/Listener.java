package pt.com.broker.ws.models;
import pt.com.broker.types.MessageListener;

import java.net.InetSocketAddress;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Copyright (c) 2014, SAPO
 * All rights reserved.
 * <p/>
 * Created by Luis Santos<luis.santos@telecom.pt> on 24-06-2014.
 */
public class Listener {
	
	private String hostName;
	private int port;
	
	public Listener(){
		
	}
	
	public String getHostName() {
		return hostName;
	}
	
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	
	public int getPort() {
		return port;
	}
	
	public void setPort(int port) {
		this.port = port;
	}

//    private final MessageListener messageListener;
//
//    private final InetSocketAddress socketAddress;
//
//    public Listener(MessageListener messageListener) {
//        this.messageListener = messageListener;
//
//        socketAddress = (InetSocketAddress) messageListener.getChannel().getChannel().remoteAddress();
//    }
//
//    @JsonProperty("port")
//    public int getPort() {
//        return socketAddress.getPort();
//    }
//
//
//    @JsonProperty("host")
//    public String getHostname() {
//        return socketAddress.getHostString();
//    }

}
