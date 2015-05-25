package pt.com.gcs.messaging;

import java.util.Set;

import pt.com.broker.types.MessageListener;

/**
 * Created by luissantos on 24-06-2014.
 */
public interface SubscriptionProcessor
{

	public String getSubscriptionName();

	public Set<MessageListener> localListeners();

	public Set<MessageListener> remoteListeners();

	public boolean hasLocalListeners();

	public boolean hasRemoteListeners();

}
