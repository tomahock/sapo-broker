package pt.com.gcs.net.stats;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class SubscriptionInfo
{

	private final String subscriptionName;
	private final List<InetSocketAddress> localListener;
	private final List<InetSocketAddress> remoteListener;

	public SubscriptionInfo(String subscriptionName)
	{
		this.subscriptionName = subscriptionName;
		this.localListener = new ArrayList<InetSocketAddress>();
		this.remoteListener = new ArrayList<InetSocketAddress>();
	}

	public String getSubscriptionName()
	{
		return subscriptionName;
	}

	public void addLocalListener(InetSocketAddress address)
	{
		localListener.add(address);
	}

	public void addRemoteListener(InetSocketAddress address)
	{
		remoteListener.add(address);
	}

	public List<InetSocketAddress> getLocalListeners()
	{
		return localListener;
	}

	public List<InetSocketAddress> getRemoteListeners()
	{
		return remoteListener;
	}

}
