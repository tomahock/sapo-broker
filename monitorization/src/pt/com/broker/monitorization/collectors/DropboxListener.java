package pt.com.broker.monitorization.collectors;

public interface DropboxListener
{
	void onUpdate(String agentName, String dropboxLocation, int messages, int goodMessages);
}
