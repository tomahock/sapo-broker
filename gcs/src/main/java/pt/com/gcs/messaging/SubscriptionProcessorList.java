package pt.com.gcs.messaging;

import java.util.Collection;

/**
 * Created by luissantos on 24-06-2014.
 */
public interface SubscriptionProcessorList
{

	public SubscriptionProcessor getSubscriptionProcessor(String name);

	public Collection<SubscriptionProcessor> getValues();

}
