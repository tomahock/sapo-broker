
using System;
using System.Threading;
using System.Collections.Generic;

using SapoBrokerClient;
using SapoBrokerClient.Utils;
using SapoBrokerClient.Encoding;
using SapoBrokerClient.Networking;
using SapoBrokerClient.Authentication;

namespace SapoBrokerClient.Messaging
{
    public delegate void ExceptionHandler(Exception exception);
    public delegate void FaultHandler(NetFault fault);
    public delegate void PongHandler(NetPong pong);
    public delegate void CommunicationFailed();
    
    public class BrokerProtocolHandler
	{
        private struct PollRequest
        {
            public NetMessage Subscription;
            public HandoverSyncObject<NetNotification> Handover;
        }

		private static readonly log4net.ILog log = log4net.LogManager.GetLogger(System.Reflection.MethodBase.GetCurrentMethod().DeclaringType);

        public static NetNotification UnblockNotification = new NetNotification("UnblockNotification", NetAction.DestinationType.QUEUE, null, null, null);
        public static NetNotification NoMessageNotification = new NetNotification("NoMessagekNotification", NetAction.DestinationType.QUEUE, null, null, null);
		
		#region Private data members
		private NetworkHandler networkHandler;
		private IMessageSerializer messageSerializer;
		
        //private IDictionary<string, Subscription> subscriptions = new Dictionary<string, Subscription>();
        private IDictionary<NetAction.DestinationType, IDictionary<string, Subscription>> subscriptions = new Dictionary<NetAction.DestinationType, IDictionary<string, Subscription>>();

        private IDictionary<string, PollRequest> syncSubscriptions = new Dictionary<string, PollRequest>();

        // Send-related fieds. Ensures that only one message is sent at a time and ensures that no message are sent during reconnect.
        private object sendLock = new Object(); // send-related operations should lock/sync in this object.
        private volatile bool sendSuspended = false; // informs is send is suspended (reconnecting) or not.
        private volatile bool sendOk; // after failure, is it ok to send message?

		#endregion
				
		#region Auxiliar methods
		
		private byte[] EncodeMessage(NetMessage message)
		{
			return messageSerializer.Marshall(message);
		}
		
		private NetMessage DecodeMessage(byte[] encodedData)
		{
			return messageSerializer.Unmarshall(encodedData);
		}

        private void DealWithException(Exception exception)
		{
            log.Error("Error", exception);
            ExceptionHandler handler = OnException;
            if (handler != null)
                OnException(exception);
		}

		#endregion


        #region Delegates and Events

        public event ExceptionHandler OnException;
        public event FaultHandler OnFault;
        public event PongHandler OnPong;
        public event CommunicationFailed OnCommunicationFailed;

        #endregion

        #region Constructors
        public BrokerProtocolHandler(IMessageSerializer messageSerializer, NetworkHandler networkHandler)
		{
			this.messageSerializer = messageSerializer;
            this.networkHandler = networkHandler;

            subscriptions.Add(NetAction.DestinationType.TOPIC, new Dictionary<string, Subscription>());
            subscriptions.Add(NetAction.DestinationType.QUEUE, new Dictionary<string, Subscription>());
			
			networkHandler.MessageReceived += delegate(byte[] encodedData) {
				try{
					NetMessage netMessage = DecodeMessage(encodedData);
					
					HandleIncommingMessage(netMessage);					
				}catch(Exception ex){
					DealWithException(ex);
				}
			};

            networkHandler.IoFailed += new NetworkHandler.IoFailureHandler(IoFailHandler);
			
			networkHandler.Start();
		}

        
		
		#endregion

        private void SendSubscriptions()
        {
            lock (this)
            {
                //IDictionary<NetAction.DestinationType, IDictionary<string, Subscription>>
                foreach (KeyValuePair<NetAction.DestinationType, IDictionary<string, Subscription>> destinations in subscriptions)
                {
                    foreach (KeyValuePair<string, Subscription> subscription in destinations.Value)
                    {
                        this.HandleOutgoingMessage(subscription.Value.ToNetMessage(), null);
                    }
                }

                lock (syncSubscriptions)
                {
                    foreach (KeyValuePair<string, PollRequest> request in syncSubscriptions)
                    {
                        this.HandleOutgoingMessage(request.Value.Subscription, null);
                    }
                }
            }
        }

        private void IoFailHandler(NetworkHandler.IoSyncStatus syncStatus)
        {
            sendSuspended = true;
            sendOk = false;
            
            syncStatus.OnChange.OnEvent += delegate(NetworkHandler.IoStatus status)
            {
                if (status == NetworkHandler.IoStatus.Ok)
                {
                    log.Info("Connection re-established");

                    lock (sendLock)
                    {
                        this.sendSuspended = false;
                        Monitor.PulseAll(sendLock);
                    }

                    if (usingAuth)
                    {
                        lock (this)
                        {
                            Authenticate(this.provider);
                        }
                    }

                    SendSubscriptions();
                }
                else
                {
                    log.Error("Communication Failed");
                    
                    if( OnCommunicationFailed != null)
						OnCommunicationFailed();
                }
            };
        }
		
		#region Incomming messages
		
		public void HandleIncommingMessage(NetMessage message)
		{
            try{
				switch( message.Action.Action )
				{
				case NetAction.ActionType.ACCEPTED:
					PendingAcceptRequestsManager.MessageReceived(message.Action.AcceptedMessage.ActionId);
					break;
				case NetAction.ActionType.FAULT:
                    HandleFault(message);
                    break;
				case NetAction.ActionType.NOTIFICATION:
					HandleNotification(message);
					break;
				case NetAction.ActionType.PONG:
                    HandlePongMessage(message);
					break;
				default:
					DealWithException( new Exception(String.Format("Unexpected incoming message type: {0}", message.Action.Action)));
					break;
				}
			} catch(Exception ex) {
				DealWithException( new Exception("Error handling incomming message.", ex));
			}
		}
		
		private void HandleNotification(NetMessage message)
		{
			string subscription = message.Action.NotificationMessage.Subscription;

            NetAction.DestinationType destType = message.Action.NotificationMessage.DestinationType;

            if (destType != NetAction.DestinationType.TOPIC)
            {
                lock (this.syncSubscriptions)
                {
                    string subs = message.Action.NotificationMessage.Subscription;
                    if (syncSubscriptions.ContainsKey(subscription))
                    {
                        PollRequest request = syncSubscriptions[subs];
                        request.Handover.Offer(message.Action.NotificationMessage);
                        return;
                    }
                }
            }

            lock (subscriptions)
            {
                IDictionary<string, Subscription> destSubscriptions = subscriptions[destType == NetAction.DestinationType.TOPIC ? NetAction.DestinationType.TOPIC : NetAction.DestinationType.QUEUE];

                if (destSubscriptions.ContainsKey(subscription))
                {
                    destSubscriptions[subscription].FireOnMessage(message.Action.NotificationMessage);
                }
                else
                {
                    DealWithException(new UnexpectedMessageException("No registered subscribers for received message.", message));
                }
            }
		}

        private void HandlePongMessage(NetMessage message)
        {
            PongHandler handler = OnPong;
            if (handler != null)
                OnPong(message.Action.PongMessage);
        }

        private void HandleFault(NetMessage message)
        {
            string msgDest = message.Action.FaultMessage.Detail;

            if( message.Action.FaultMessage.Code.Equals( NetFault.PollTimeoutErrorMessage.Action.FaultMessage.Code ) ||
                message.Action.FaultMessage.Code.Equals(NetFault.NoMessageInQueueErrorMessage.Action.FaultMessage.Code) )
            {
                lock (this.syncSubscriptions)
                {
                    if (syncSubscriptions.ContainsKey(msgDest))
                    {
                        PollRequest request = syncSubscriptions[msgDest];
                        if( message.Action.FaultMessage.Code.Equals( NetFault.PollTimeoutErrorMessage.Action.FaultMessage.Code ) )
                        {
                            request.Handover.Offer(UnblockNotification);
                        }
                        else
                        {
                            request.Handover.Offer(NoMessageNotification);
                        }
                    }
                    return;
                }
            }

            message.Action.FaultMessage.Headers = message.Headers;

            if (!PendingAcceptRequestsManager.MessageFailed(message.Action.FaultMessage))
            {
                FaultHandler handler = OnFault;
                if (handler != null)
                    OnFault(message.Action.FaultMessage);
            }
        }

        
		
		#endregion
		
		#region Outgoing messages

        public static void SendMessageOverUdp(NetMessage message, HostInfo hostInfo, IMessageSerializer messageSerializer)
        {
            byte[] encodedData = messageSerializer.Marshall(message);
            UdpNetworkHandler.SendMessage(encodedData, hostInfo, messageSerializer);
        }

		public bool HandleOutgoingMessage(NetMessage message, AcceptRequest acceptRequest)
		{
			if( acceptRequest != null)
			{
                PendingAcceptRequestsManager.AddAcceptRequest(acceptRequest);
			}

            // transform and Marshall message
            byte[] encodedData = EncodeMessage(message);

            return SendMessage(encodedData);
        }

        private bool SendMessage(byte[] encoded)
        {
            lock (sendLock)
            {
                if(sendSuspended)
                {
                    Monitor.Wait(sendLock);
                    if (!this.sendOk)
                        return false;
                }
            }

            // send message
            return networkHandler.SendMessage(encoded, this.messageSerializer);
        }


        #endregion

        private ICredentialsProvider provider;
        private AuthenticationInfo clientAuthInfo;
        private volatile bool usingAuth = false;


        public bool Authenticate(ICredentialsProvider provider)
        {
            log.Info("Authenticating");
            if (provider == null)
            {
                throw new ArgumentNullException("AuthenticationInfo can not be null in order to authenticate.");
            }
            AuthenticationInfo authInfoToUse = null;
            try
            {
                authInfoToUse = provider.GetCredentials();
                if (authInfoToUse == null)
                    throw new InvalidCredentialsException("Credential provider returned null");
            }
            catch (InvalidCredentialsException ice)
            {
                log.Error("Failed to obtain credentials.", ice);
                return false;
            }

            // save important information
            lock (this)
            {
                this.provider = provider;
                this.clientAuthInfo = authInfoToUse;
                this.usingAuth = true;
            }

            // build NetMessage

            string actionId = System.Guid.NewGuid().ToString();

            NetAuthentication netAuth = new NetAuthentication(authInfoToUse.Token, authInfoToUse.UserAuthenticationType);
            if ((authInfoToUse.Roles != null) && (authInfoToUse.Roles.Count != 0))
                netAuth.Roles = authInfoToUse.Roles;
            if (authInfoToUse.UserId != null)
            {
                netAuth.UserId = authInfoToUse.UserId;
            }
            netAuth.ActionId = actionId;

            NetAction netAction = new NetAction(NetAction.ActionType.AUTH);
            netAction.AuthenticationMessage = netAuth;
            NetMessage msg = new NetMessage(netAction);

            // build waitable object
            WaitMessageAccepted waitMsgAccepted = new WaitMessageAccepted();
            AcceptRequest acceptRequest = new AcceptRequest(actionId, waitMsgAccepted, 7000);
            
            //send message
            HandleOutgoingMessage(msg, acceptRequest);

            // wait for response
            lock (waitMsgAccepted.SyncObject)
            {
                Monitor.Wait(waitMsgAccepted.SyncObject);
            }
            if (waitMsgAccepted.WaitResult != WaitMessageAccepted.Result.Accepted)
            {
                log.Error("Authenticatation failed. Reason: " + waitMsgAccepted.WaitResult);
                return false;
            }

            log.Info("Authenticated");

            return true;
        }

		
		#region Other
		
		public void AddSubscription(Subscription subscription)
		{
			lock(subscriptions){
                IDictionary<string, Subscription> destSubscriptions = subscriptions[subscription.DestinationType == NetAction.DestinationType.TOPIC ? NetAction.DestinationType.TOPIC : NetAction.DestinationType.QUEUE];
                destSubscriptions.Add(subscription.DestinationPattern, subscription);
			}
		}
		
		public void RemoveSubscription(Subscription subscription)
		{
			lock(subscriptions){
                IDictionary<string, Subscription> destSubscriptions = subscriptions[subscription.DestinationType == NetAction.DestinationType.TOPIC ? NetAction.DestinationType.TOPIC : NetAction.DestinationType.QUEUE];
                destSubscriptions.Remove(subscription.DestinationPattern);
			}
		}

        //syncSubscriptions

        internal NetNotification GetSyncMessage(string queueName, NetMessage message)
        {
            NetNotification receivedMessage = null;
            HandoverSyncObject<NetNotification> synObj = null;
            lock (syncSubscriptions)
            {
                if (syncSubscriptions.ContainsKey(queueName))
                    throw new ArgumentException("Queue " + queueName + " has already a poll runnig.");
                PollRequest pr = new PollRequest();
                pr.Subscription = message;
                pr.Handover = synObj = new HandoverSyncObject<NetNotification>();
                syncSubscriptions.Add(queueName, pr);
            }

            receivedMessage = synObj.Get();
            lock (syncSubscriptions)
            {
                syncSubscriptions.Remove(queueName);
            }

            return receivedMessage;
        }

        public int ReconnectionRetries
        {
            get { return networkHandler.ReconnectionRetries; }
            set { networkHandler.ReconnectionRetries = value; }
        }

        public void Close()
        {
            networkHandler.Close();
        }

		#endregion
    }
	
}
