package pt.com.broker.security;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;

import javax.crypto.SecretKey;

import org.apache.mina.core.session.IoSession;

import pt.com.gcs.conf.ChannelType;

public class SessionProperties extends HashMap<String, Object> {
	private static final long serialVersionUID = -5044861586951252094L;

	private InetAddress address = null;
	private List<String> roles = null;
	private List<ChannelType> channelTypes = null;
	private SecretKey key = null;

	
	public SessionProperties(IoSession session)
	{
		address = ((InetSocketAddress) session.getRemoteAddress()).getAddress();
	}
	
//	InetSocketAddress
	public void setAddress(InetAddress address)
	{
		this.address = address;
	}

	public InetAddress getAddress()
	{
		return address;
	}

	public void setRoles(List<String> roles)
	{
		this.roles = roles;
	}

	public List<String> getRoles()
	{
		return roles;
	}

	public void setChannelTypes(List<ChannelType> channelTypes)
	{
		this.channelTypes = channelTypes;
	}

	public List<ChannelType> getChannelTypes()
	{
		return channelTypes;
	}

	public void setKey(SecretKey key)
	{
		this.key = key;
	}

	public SecretKey getKey()
	{
		return key;
	}
	
}
