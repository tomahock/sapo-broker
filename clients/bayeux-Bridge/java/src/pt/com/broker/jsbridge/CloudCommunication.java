package pt.com.broker.jsbridge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.netty.handler.codec.bayeux.BayeuxData;
import org.jboss.netty.handler.codec.bayeux.BayeuxMessage;
import org.jboss.netty.handler.codec.bayeux.BayeuxRouter;
import org.jboss.netty.handler.codec.bayeux.PublishRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.client.BrokerClient;
import pt.com.broker.client.HostInfo;
import pt.com.broker.client.messaging.BrokerListener;
import pt.com.broker.jsbridge.configuration.ConfigurationInfo;
import pt.com.broker.jsbridge.configuration.Mappings.MappingSet.Mapping;
import pt.com.broker.types.NetBrokerMessage;
import pt.com.broker.types.NetNotification;
import pt.com.broker.types.NetSubscribe;
import pt.com.broker.types.NetAction.DestinationType;

public class CloudCommunication
{
	private static final Logger log = LoggerFactory.getLogger(CloudCommunication.class);
	
	private static class ChannelMapping
	{
		private final String channel;
		private final String subscription;
		private final String publication;
		private final MessageTransformer transformation;
		private final boolean publicationAllowed;
		private final boolean subscriptionAllowed; 
		

		public ChannelMapping(String channel, String subscription, String publication, MessageTransformer transformation, boolean publicationAllowed,boolean subscriptionAllowed)
		{
			this.channel = channel; 
			this.subscription = subscription;
			this.publication = publication;
			this.transformation = transformation;
			this.publicationAllowed = publicationAllowed;
			this.subscriptionAllowed = subscriptionAllowed;
		}
	
		public String getChannel()
		{
			return channel;
		}

		public String getSubscription()
		{
			return subscription;
		}
		
		public MessageTransformer getTransformation()
		{
			return transformation;
		}

		private String getPublication()
		{
			return publication;
		}

		public boolean isPublicationAllowed()
		{
			return publicationAllowed;
		}

		public boolean isSubscriptionAllowed()
		{
			return subscriptionAllowed;
		}

	}
	
	private final List<HostInfo> hostInfo;
	private final String cloudName;
	private final String mappingsSetName;
	
	private BrokerClient brokerClient;

	private Map<String, ChannelMapping> messagePublication = new HashMap<String, ChannelMapping>(); // used in publication and subscription
	private Map<String, ChannelMapping> messageNotifications  = new HashMap<String, ChannelMapping>(); // used by notifications

	private Map<String, List<String>> subscriptions = new HashMap<String, List<String> >();
	private static final AtomicInteger counter = new AtomicInteger(0);
	
	public CloudCommunication(List<HostInfo> hostInfo, String cloudName, String mappingsSetName)
	{
		this.hostInfo = hostInfo;
		this.cloudName = cloudName;
		this.mappingsSetName = mappingsSetName;
	}
	
	public synchronized boolean init()
	{
		
		System.out.println("CloudCommunicationManager initializing: " + getCloudName());
		
		// Instantiate broker client
		try{
			brokerClient = new BrokerClient(this.hostInfo);
		}catch (Throwable t) {
			log.error("Failed to create BrokerClient. Cloud name: " + getCloudName(), t);
			return false;
		}
		
		// Get mappings
		List<Mapping> mappingSet = ConfigurationInfo.getMappingSet(mappingsSetName);
		for(Mapping mapping : mappingSet)
		{
			try{
				MessageTransformer notificationTransformation = null; 
				if( mapping.getFromAgentTransformationClass() != null)
				{
					notificationTransformation = (MessageTransformer)Class.forName(mapping.getFromAgentTransformationClass()).newInstance();
					notificationTransformation.init();
				} 
								
				MessageTransformer publicationTransformation = null;
				if(mapping.getToAgentTransformationClass() !=  null)
				{
					publicationTransformation= (MessageTransformer)Class.forName( mapping.getToAgentTransformationClass() ).newInstance();
					publicationTransformation.init();
				}
				
				ChannelMapping translation = new ChannelMapping(mapping.getChannel(), mapping.getSubscription(), mapping.getPublication(), notificationTransformation, !mapping.isDenyPublication(), !mapping.isDenySubscription());
				messagePublication.put(mapping.getChannel(), translation);
				messageNotifications.put( mapping.getSubscription(), translation );
				
				if(!mapping.isDenyPublication())
					ConfigurationInfo.addPublicationChannel(mapping.getChannel());
				
				if(!mapping.isDenySubscription())
					ConfigurationInfo.addSubscriptionChannel(mapping.getChannel());
				
			}
			catch(Throwable t)
			{
				log.error(this.getClass().getName() + " named '" + getCloudName() + "' failed no initalize.", t);
				brokerClient.close();
				return false;
			}
		}		
		return true;
	}
	
	public boolean publish(String channel, String message)
	{
		if( ! messagePublication.containsKey(channel) )
		{
			return false;
		}
		
		ChannelMapping translation = messagePublication.get(channel);
		
		if(!translation.isPublicationAllowed())
			return false;
		
		NetBrokerMessage brokerMessage = new NetBrokerMessage(message.getBytes());

		NetBrokerMessage transformedMessage = brokerMessage;
		MessageTransformer transformation = translation.getTransformation();
		if(transformation != null)
		{
			transformedMessage = transformation.transform(brokerMessage);
		}
		
		if(transformedMessage == null)
			return false;
		
		brokerClient.publishMessage(transformedMessage, translation.getPublication());
		
		return true;
	}
	
	public boolean registerChannel(String channel, String clientId)
	{
		ChannelMapping channelTranslation = messagePublication.get(channel);
		
		if( channelTranslation == null )
		{
			return false;
		}
		
		if(!channelTranslation.isSubscriptionAllowed())
			return false;
		
		synchronized (subscriptions)
		{
			List<String> clients = subscriptions.get(channel);
			if (clients != null)
			{
				if(!clients.contains(clientId))
					clients.add(clientId);
				return true;
			}
			else
			{
				clients = new ArrayList<String>();
				clients.add(clientId);
				
				subscriptions.put(channel, clients);
			}
		}
		
		NetSubscribe subscribe = new NetSubscribe(channelTranslation.getSubscription(), DestinationType.TOPIC);

		try
		{
			brokerClient.addAsyncConsumer(subscribe, new BrokerListener()
			{

				@Override
				public boolean isAutoAck()
				{
					return false;
				}

				@Override
				public void onMessage(NetNotification message)
				{
				
					ChannelMapping translation = null;
					synchronized(messageNotifications)
					{
						translation = messageNotifications.get(message.getSubscription());
					}

					if(translation == null)
						return;

					MessageTransformer transformation = translation.getTransformation();
					NetBrokerMessage transformedMessage = message.getMessage();
					if(transformation != null)
					{
						transformedMessage = transformation.transform(transformedMessage);
					}
					
					if( transformedMessage == null )
						return;
		
					PublishRequest publishRequest = new PublishRequest(new BayeuxMessage());
					publishRequest.setChannel(translation.getChannel());
					publishRequest.setClientId("Sapo-Broker Javascript Bridge");
					publishRequest.setId(counter.addAndGet(1)+"" );
					publishRequest.setData(new BayeuxData());
					publishRequest.getData().put("subscription", translation.getChannel());
					publishRequest.getData().put("data", new String(transformedMessage.getPayload()));

					BayeuxRouter.getInstance().onPublish(publishRequest);

					/*************************************************/

//					Dai Jun's alternative					
					
					
//					if(false)
//					{
//						BayeuxConnection connection=(BayeuxConnection)e.getMessage();//e is a MessageEvent instance from lower lever BayeuxDecoder
//						BayeuxMessage bayeux=connection.pollFromUpstream();
//						while(bayeux!=null){
//						  PublishRequest request=(PublishRequest)bayeux;//Suppose it's a PublishRequest instance here, usually you should use "instanceof" first.
//						  if(a few modifications to request){//Mainly use following	BayeuxEncoder to handle it according to protocol
//						      BayeuxData data=request.getData();
//						      /* changes to data */
//						      BayeuxExt ext=request.getExt();
//						      /* handle ext */
//						      connection.receiveToQueue(request);
//						  }else{ //Custom logic to reponse
//						      BayeuxData data=request.getData();
//						      /* changes to data */
//						      BayeuxExt ext=request.getExt();
//						      /* handle ext */
//						     PublishResponse response=new PubllishResponse(request);
//						      /* settings to response */
//						     connection.sendToQueue(response);//Or use connection.send(response) to send messages immediatly.
//						  }
//						  bayeux=connection.pollFromUpstream();
//						}
//
//					}
				}
			});
		}
		catch (Throwable t)
		{
			log.error("Error while processing notification.", t);
		}
		return true;
	}
	
	public void unregisterChannel(String channel, String clientId)
	{
		try
		{
			synchronized (subscriptions)
			{
				List<String> clients = subscriptions.get(channel);
				if (clients == null)
				{
					return;
				}
				if (clients.size() != 1)
				{
					clients.remove(channel);
					return;
				}
				else
				{
					subscriptions.remove(channel);
				}
			}
			brokerClient.unsubscribe(DestinationType.TOPIC, channel);
		}
		catch (Throwable t)
		{
			log.error("Error while processing unregistering channel.", t);
		}
	}
	
	public void unregisterClient(String clientId)
	{
		List<String> forRemoval = new ArrayList<String>();
		synchronized (subscriptions)
		{
			for(String channel : subscriptions.keySet())
			{
				if(subscriptions.get(channel).contains(clientId))
					forRemoval.add(channel);
			}
		}
		for(String channel : forRemoval)
		{
			unregisterChannel(channel, clientId);
		}
	}

	public String getCloudName()
	{
		return cloudName;
	}	
}
