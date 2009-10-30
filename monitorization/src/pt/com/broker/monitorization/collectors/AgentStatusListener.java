package pt.com.broker.monitorization.collectors;

public interface AgentStatusListener
{
	void onUpdate(String agentName, AgentStatus status);
}
