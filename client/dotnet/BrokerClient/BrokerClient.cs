
using System;
using System.Threading;
using System.Collections.Generic;

using SapoBrokerClient.Utils;
using SapoBrokerClient.Encoding;
using SapoBrokerClient.Messaging;
using SapoBrokerClient.Networking;

namespace SapoBrokerClient
{
	public class BrokerClient : IDisposable
	{
		private static readonly log4net.ILog log = log4net.LogManager.GetLogger(System.Reflection.MethodBase.GetCurrentMethod().DeclaringType);
		
		private static IMessageSerializer defaultMessageSerializer = new SapoBrokerClient.Encoding.Thrift.ThriftMessageSerializer();

        public static IMessageSerializer DefaultMessageSerializer
        {
            get { return defaultMessageSerializer; }
        }

        private static int defaultReconnectionRetries = int.MaxValue;

        /// <summary>
        /// Default reconnection retries. Initial value is int.MaxValue.
        /// </summary>
        public static int DefaultReconnectionRetries
        {
            get { return defaultReconnectionRetries; }
        }
        /// <summary>
        /// Determines how many times the client will try to reconnect to the agents. Default is DefaultReconnectionRetries.
        /// </summary>
        public int ReconnectionRetries
        {
            get { return reconnectionRetries; }
            set { 
                reconnectionRetries = value;
                this.protocolHandler.ReconnectionRetries = reconnectionRetries;
            }
        }

		#region Private members
		
		protected IList<HostInfo> hosts;
		protected BrokerProtocolHandler protocolHandler;
		protected IMessageSerializer messageSerializer = defaultMessageSerializer;
        protected bool closed = false;
        private int reconnectionRetries = DefaultReconnectionRetries;

		#endregion

        #region Events

        /// <summary>
        /// Event fired when a Fault message is received.
        /// </summary>
        public event FaultHandler OnFault
        {
            add
            {
                protocolHandler.OnFault += value;
            }
            remove
            {
                protocolHandler.OnFault -= value;
            }

        }
        /// <summary>
        /// Event fired when an messageing execption occurs (e.g. Mal-formed message received).
        /// </summary>
        public event ExceptionHandler OnException
        {
            add
            {
                protocolHandler.OnException += value;
            }
            remove
            {
                protocolHandler.OnException -= value;
            }
        }
        /// <summary>
        /// Communication failed and it was not possible to reconnect. Sugestion: Suply several host information (broker agents).
        /// </summary>
        public event CommunicationFailed OnCommunicationFailed
        {
            add
            {
                protocolHandler.OnCommunicationFailed += value;
            }
            remove
            {
                protocolHandler.OnCommunicationFailed -= value;
            }
        }

        #endregion

        protected BrokerClient()
        {
        }

        /// <summary>
        /// Creates a BrokerClient instance and connects to an agent.
        /// </summary>
        /// <param name="hostInfo">Information about an agent.</param>
        public BrokerClient(HostInfo hostInfo)
		{
            IList<HostInfo> hosts = new List<HostInfo>(1);
            hosts.Add(hostInfo);

            this.hosts = hosts;
            NetworkHandler networkHandler = new NetworkHandler(hosts);
            protocolHandler = new BrokerProtocolHandler(messageSerializer, networkHandler);
            protocolHandler.OnCommunicationFailed += new CommunicationFailed(HandleOnCommunicationFailed);
		}
                
        /// <summary>
        /// Creates a BrokerClient instance and connects to an agent.
        /// </summary>
        /// <param name="hosts">Information about agents.</param>
        public BrokerClient(IList<HostInfo> hosts)
        {
            this.hosts = hosts;
            NetworkHandler networkHandler = new NetworkHandler(hosts);
            protocolHandler = new BrokerProtocolHandler(messageSerializer, networkHandler);
        }
		
		/// <summary>
		/// Enqueue a message.
		/// </summary>
		/// <param name="message">A broker message</param>
		/// <param name="destination">A destination (e.g. "/queue/foo")</param>
		public void Enqueue(NetBrokerMessage message, string destination)
		{
			Enqueue(message, destination, null);		
		}
		
        /// <summary>
        /// Enqueue a message.
        /// </summary>
        /// <param name="message">A broker message</param>
        /// <param name="destination">A destination (e.g. "/queue/foo")</param>
        /// <param name="acceptRequest">An AcceptRequest instance.</param>
		public void Enqueue(NetBrokerMessage message, string destination, AcceptRequest acceptRequest)
		{
            if (IsClosed())
                return;

            NetPublish publish = new NetPublish(destination, NetAction.DestinationType.QUEUE, message);
			
			NetAction action = new NetAction(NetAction.ActionType.PUBLISH);
			action.PublishMessage = publish;
			
			NetMessage netMessage = new NetMessage(action, message.Headers);
			
			protocolHandler.HandleOutgoingMessage(netMessage, acceptRequest);			
		}
		/// <summary>
		/// Publish a message (to a topic).
		/// </summary>
        /// <param name="message">A broker message</param>
        /// <param name="destination">A destination (e.g. "/topic/foo")</param>
		public void Publish(NetBrokerMessage message, string destination)
		{
			Publish(message, destination, null);
		}
		/// <summary>
        /// Publish a message (to a topic).
		/// </summary>
        /// <param name="message">A broker message</param>
        /// <param name="destination">A destination (e.g. "/topic/foo")</param>
        /// <param name="acceptRequest">An AcceptRequest instance.</param>
		public void Publish(NetBrokerMessage message, string destination, AcceptRequest acceptRequest)
		{
            if (IsClosed())
                return;

			NetPublish publish = new NetPublish(destination, NetAction.DestinationType.TOPIC, message);
			
			NetAction action = new NetAction(NetAction.ActionType.PUBLISH);
			action.PublishMessage = publish;
			
			NetMessage netMessage = new NetMessage(action, message.Headers);
            
			protocolHandler.HandleOutgoingMessage(netMessage, acceptRequest);
		}
		/// <summary>
		/// Subscribe to a destination.
		/// </summary>
        /// <param name="subscription">A Subscription instance.</param>
		public void Subscribe(Subscription subscription)
		{
			Subscribe(subscription, null);
		}
		/// <summary>
        /// Subscribe to a destination.
		/// </summary>
        /// <param name="subscription">A Subscription instance.</param>
        /// <param name="acceptRequest">An AcceptRequest instance.</param>
		public void Subscribe(Subscription subscription, AcceptRequest acceptRequest)
		{
            if (IsClosed())
                return;

            subscription.BrokerClient = this;

			NetSubscribe netSubscribe = new NetSubscribe(subscription.DestinationPattern, subscription.DestinationType);
			NetAction action = new NetAction(NetAction.ActionType.SUBSCRIBE);
			action.SubscribeMessage = netSubscribe;
			NetMessage netMessage = new NetMessage(action, subscription.Headers);
            
			protocolHandler.HandleOutgoingMessage(netMessage, acceptRequest);
			
			protocolHandler.AddSubscription(subscription);
		}
		/// <summary>
        /// Cancel a previous subscription.
		/// </summary>
        /// <param name="subscription">A Subscription instance.</param>
		public void Unsubscribe(Subscription subscription)
		{
			Unsubscribe(subscription, null);
		}
		/// <summary>
        /// Cancel a previous subscription.
		/// </summary>
        /// <param name="subscription">A Subscription instance.</param>
        /// <param name="acceptRequest">An AcceptRequest instance.</param>
		public void Unsubscribe(Subscription subscription, AcceptRequest acceptRequest)
		{
            if (IsClosed())
                return;

			NetUnsubscribe netUnsubscribe = new NetUnsubscribe(subscription.DestinationPattern, subscription.DestinationType);
			NetAction action = new NetAction(NetAction.ActionType.UNSUBSCRIBE);
			action.UnsbuscribeMessage = netUnsubscribe;
			NetMessage netMessage = new NetMessage(action, subscription.Headers);
			
			protocolHandler.HandleOutgoingMessage(netMessage, acceptRequest);
			
			protocolHandler.RemoveSubscription(subscription);
		}
        /// <summary>
        /// Poll an message from a queue.
        /// </summary>
        /// <param name="queueName">Queue name (e.g. "/queue/foo").</param>
        /// <returns>A NetNotification instance. In case of connection fail or if there are no messages in local agent's queue when timeout is negative null is returned.</returns>
        public NetNotification Poll(String queueName)
        {
            return Poll(queueName, 0, -1, null);
        }
        /// <summary>
        /// Poll an message from a queue.
        /// </summary>
        /// <param name="queueName">Queue name (e.g. "/queue/foo").</param>
        /// <param name="timeout">Time, in miliseconds, before the agent stops waiting for a message, if the timeout is bigger than 0. If timeout is reached a TimeoutException is thrown. If the value is zero, than the agent will wait forever. A negative value means that the client doesn't want to wait if there are no messages is local agent's queue.</param>
        /// <returns>A NetNotification instance. In case of connection fail or if there are no messages in local agent's queue when timeout is negative null is returned.</returns>
        public NetNotification Poll(String queueName, long timeout)
        {
            return Poll(queueName, timeout, -1, null);
        }
        /// <summary>
        /// Poll an message from a queue.
        /// </summary>
        /// <param name="queueName">Queue name (e.g. "/queue/foo").</param>
        /// <param name="timeout">Time, in miliseconds, before the agent stops waiting for a message, if the timeout is bigger than 0. If timeout is reached a TimeoutException is thrown. If the value is zero, than the agent will wait forever. A negative value means that the client doesn't want to wait if there are no messages is local agent's queue.</param>
        /// <param name="acceptRequest">An AcceptRequest instance.</param>
        /// <returns>A NetNotification instance. In case of connection fail or if there are no messages in local agent's queue when timeout is negative null is returned.</returns>
        public NetNotification Poll(String queueName, long timeout, AcceptRequest acceptRequest)
        {
            if (IsClosed())
                return null;

            NetPoll poll = new NetPoll(queueName, timeout);
            NetAction action = new NetAction(NetAction.ActionType.POLL);
            action.PollMessage = poll;
            NetMessage netMessage = new NetMessage(action);

            protocolHandler.HandleOutgoingMessage(netMessage, acceptRequest);
            NetNotification notification = protocolHandler.GetSyncMessage(queueName, netMessage);
            

            if (notification == BrokerProtocolHandler.UnblockNotification)
                throw new TimeoutException();
            
            if (notification == BrokerProtocolHandler.NoMessageNotification)
                return null;

            return notification;
        }

        /// <summary>
        /// Poll an message from a queue.
        /// </summary>
        /// <param name="queueName">Queue name (e.g. "/queue/foo").</param>
        /// <param name="timeout">Time, in miliseconds, before the agent stops waiting for a message, if the timeout is bigger than 0. If timeout is reached a TimeoutException is thrown. If the value is zero, than the agent will wait forever. A negative value means that the client doesn't want to wait if there are no messages is local agent's queue.</param>
        /// <param name="reserveTime">Message reserve time, in milliseconds. Polled messages are reserved, by default, for 15 minutes. If clients prefer a different reserve time, bigger or small, they can specify it.</param>
        /// <param name="acceptRequest">An AcceptRequest instance.</param>
        /// <returns>A NetNotification instance. In case of connection fail or if there are no messages in local agent's queue when timeout is negative null is returned.</returns>
        public NetNotification Poll(String queueName, long timeout, long reserveTime,AcceptRequest acceptRequest)
        {
            if (IsClosed())
                return null;

            NetPoll poll = new NetPoll(queueName, timeout);
            NetAction action = new NetAction(NetAction.ActionType.POLL);
            action.PollMessage = poll;
            NetMessage netMessage = new NetMessage(action);

            if (reserveTime != -1)
            {
                netMessage.Headers.Add("RESERVE_TIME", reserveTime.ToString());
            }

            protocolHandler.HandleOutgoingMessage(netMessage, acceptRequest);
            NetNotification notification = protocolHandler.GetSyncMessage(queueName, netMessage);


            if (notification == BrokerProtocolHandler.UnblockNotification)
                throw new TimeoutException();

            if (notification == BrokerProtocolHandler.NoMessageNotification)
                return null;

            return notification;
        }

		/// <summary>
        /// Acknowledge a queue message.
        /// </summary>
        /// <param name="notification">The received notification object</param>
		public void Acknowledge(NetNotification notification)
        {
			if(notification.DestinationType != NetAction.DestinationType.TOPIC)
			{
            	Acknowledge(notification.Destination, notification.Message.MessageId, null);
			}
        }
		
        /// <summary>
        /// Acknowledge a queue message.
        /// </summary>
        /// <param name="quequeName">Queue name (e.g. "/queue/foo" or "myQueue@/topic/foo").</param>
        /// <param name="messageId">The received message identifier.</param>
        public void Acknowledge(string quequeName, string messageId)
        {
            Acknowledge(quequeName, messageId, null);
        }

        /// <summary>
        /// Acknowledge a queue message.
        /// </summary>
        /// <param name="quequeName">Queue name (e.g. "/queue/foo" or "myQueue@/topic/foo").</param>
        /// <param name="messageId">The received message identifier.</param>
        /// <param name="acceptRequest">An AcceptRequest instance.</param>
        public void Acknowledge(string quequeName, string messageId, AcceptRequest acceptRequest)
        {
            if (IsClosed())
                return;
            NetAcknowledge netAck = new NetAcknowledge(quequeName, messageId);
            NetAction action = new NetAction(NetAction.ActionType.ACKNOWLEDGE);
            action.AcknowledgeMessage = netAck;
            NetMessage netMessage = new NetMessage(action);

            protocolHandler.HandleOutgoingMessage(netMessage, acceptRequest);
        }
        /// <summary>
        /// Send a Ping message to determine agent liveness.
        /// </summary>
        /// <returns>A Pong message or null if conection failed.</returns>
        public NetPong Ping()
        {
            if (IsClosed())
                return null;

            NetPong localNetPong = null;

            string actionId = Guid.NewGuid().ToString();
            NetPing ping = new NetPing(actionId);
            NetAction action = new NetAction(NetAction.ActionType.PING);
            action.PingMessage = ping;
            NetMessage netMessage = new NetMessage(action);

            ManualResetEvent mrEvent = new ManualResetEvent(false);
            
            PongHandler handler = delegate(NetPong pong)
            {
                if (pong.ActionId.Equals(actionId))
                {
                    localNetPong = pong;
                    mrEvent.Set();
                }
            };
            protocolHandler.OnPong += handler;
            
            protocolHandler.HandleOutgoingMessage(netMessage, null);

            mrEvent.WaitOne(2 * 1000, false);
            protocolHandler.OnPong -= handler;

            return localNetPong;
        }

        /// <summary>
        /// Close connection.
        /// </summary>
        public void Close()
        {
            lock (this)
            {
                closed = true;
            }
            protocolHandler.Close();
        }

        private void HandleOnCommunicationFailed()
        {
            lock (this)
            {
                closed = true;
            }
        }

        private bool IsClosed()
        {
            lock (this)
            {
                return closed;
            }
        }

        /// <summary>
        /// Publish a message over UDP.
        /// </summary>
        /// <param name="message">Message content.</param>
        /// <param name="destination">Message destination.</param>
        /// <param name="hostInfo">Agent information.</param>
        /// <param name="messageSerializer">Serialization type.</param>
        public static void PublishMessageOverUdp(NetBrokerMessage message, string destination, HostInfo hostInfo, IMessageSerializer messageSerializer)
        {
            NetPublish publish = new NetPublish(destination, NetAction.DestinationType.TOPIC, message);

            NetAction action = new NetAction(NetAction.ActionType.PUBLISH);
            action.PublishMessage = publish;

            NetMessage netMessage = new NetMessage(action, message.Headers);

            BrokerProtocolHandler.SendMessageOverUdp(netMessage, hostInfo, messageSerializer);
        }

        /// <summary>
        /// Enqueue a message over UDP.
        /// </summary>
        /// <param name="message">Message content.</param>
        /// <param name="destination">Message destination.</param>
        /// <param name="hostInfo">Agent information.</param>
        /// <param name="messageSerializer">Serialization type.</param>
        public static void EnqueueMessageOverUdp(NetBrokerMessage message, string destination, HostInfo hostInfo, IMessageSerializer messageSerializer)
        {
            NetPublish publish = new NetPublish(destination, NetAction.DestinationType.QUEUE, message);

            NetAction action = new NetAction(NetAction.ActionType.PUBLISH);
            action.PublishMessage = publish;

            NetMessage netMessage = new NetMessage(action, message.Headers);

            BrokerProtocolHandler.SendMessageOverUdp(netMessage, hostInfo, messageSerializer);	
        }


        #region IDisposable Members

        public void Dispose()
        {
            Dispose(true);
        }

        protected virtual void Dispose(bool disposing)
        {
            if (disposing)
            {
                lock (this)
                {
                    if (closed)
                        return;
                    Close();
                    GC.SuppressFinalize(this);
                }
            }
        }

        ~BrokerClient()
        {
            Dispose(false);
        }

        #endregion
    }
}
