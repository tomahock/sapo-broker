package pt.com.broker.monitorization.collectors;

public interface FaultListener
{
	public void onFault(String agentName, String message);
}
