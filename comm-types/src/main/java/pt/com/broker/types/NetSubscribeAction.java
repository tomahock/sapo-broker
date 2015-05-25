package pt.com.broker.types;

import java.util.Map;

/**
 * Created by luissantos on 07-05-2014.
 */
public interface NetSubscribeAction
{

	public String getDestination();

	public NetAction.DestinationType getDestinationType();

	public String getActionId();

	public void setActionId(String actionId);

	public Map<String, String> getHeaders();

}
