package pt.com.broker.auth;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.List;

import javax.crypto.SecretKey;

import org.jboss.netty.channel.Channel;

import pt.com.gcs.conf.global.ChannelType;

/**
 * SessionProperties objects contain some client specific information such channel type, address, client roles, etc.
 * 
 */

public class SessionProperties extends HashMap<String, Object>
{
	private static final long serialVersionUID = -5044861586951252094L;

	private InetAddress address = null;
	private List<String> roles = null;
	private List<ChannelType> channelTypes = null;
	private SecretKey key = null;

	public SessionProperties(Channel session)
	{
		if (session != null)
		{
			SocketAddress remoteAddress = session.getRemoteAddress();
			if (remoteAddress != null)
			{
				address = ((InetSocketAddress) remoteAddress).getAddress();
			}
		}
	}

	// InetSocketAddress
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
