package pt.com.broker.client.nio.ignore;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.client.nio.AcceptRequest;
import pt.com.broker.client.nio.BrokerClient;
import pt.com.broker.client.nio.events.AcceptResponseListener;
import pt.com.broker.client.nio.events.NotificationListenerAdapter;
import pt.com.broker.client.nio.server.HostInfo;
import pt.com.broker.types.NetAccepted;
import pt.com.broker.types.NetFault;
import pt.com.broker.types.NetNotification;
import pt.com.broker.types.NetSubscribe;
import pt.com.broker.types.NetAction.DestinationType;

public class BrokerConsumer {
	
	static final Logger log = LoggerFactory.getLogger(BrokerConsumer.class);
	
	private BrokerClient bClient;
	
	public BrokerConsumer(String brokerHost, int brokerPort, DestinationType destinationType, String destination) throws Exception{
		bClient = new BrokerClient(brokerHost, brokerPort);
		bClient.connect();
		
		AcceptRequest request = new AcceptRequest(UUID.randomUUID().toString(),new AcceptResponseListener() {
			
            @Override
            public void onMessage(NetAccepted message, HostInfo host) {
                log.info("Success");
            }

            @Override
            public void onFault(NetFault fault, HostInfo host) {
                log.error("Fault");
            }

            @Override
            public void onTimeout(String actionID) {
                log.error("Timeout");

            }
            
        }, 10000);
		
		bClient.subscribe(new NetSubscribe(destination, destinationType), new NotificationListenerAdapter() {
			
			@Override
			public boolean onMessage(NetNotification notification, HostInfo host) {
				try {
					log.debug("Received a message: {}", new String(notification.getMessage().getPayload(), "UTF-8"));
				} catch (Exception e) {
					e.printStackTrace();
				} 
				return true;
			}
		}, request);
	}
}
