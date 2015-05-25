package pt.com.broker.ws.models;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Copyright (c) 2014, SAPO All rights reserved.
 *
 * <p/>
 * Created by Luis Santos<luis.santos@telecom.pt> on 24-06-2014.
 */
public class Subscription
{

	@JsonProperty("name")
	private String name;
	@JsonProperty("local_listeners")
	private List<Listener> localListeners;
	@JsonProperty("remote_listeners")
	private List<Listener> remoteListeners;

	public Subscription()
	{
		this.name = "";
		this.localListeners = new ArrayList<Listener>();
		this.remoteListeners = new ArrayList<Listener>();
	}

	public Subscription(String name)
	{
		this.name = name;
		this.localListeners = new ArrayList<Listener>();
		this.remoteListeners = new ArrayList<Listener>();
	}

	public String getName()
	{
		return name;
	}

	public void addLocalListener(Listener listener)
	{
		localListeners.add(listener);
	}

	public void addRemoteListener(Listener listener)
	{
		remoteListeners.add(listener);
	}

}
