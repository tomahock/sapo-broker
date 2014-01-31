
using System;
using System.Collections.Generic;

namespace SapoBrokerClient
{
	/// <summary>
	/// Subscription represents a client subscription to a TOPIC, QUEUE or VIRTUAL_QUEUE.
	/// OnMessage event is raised by the library when a message conforming to the subscription is received.
	/// </summary>
	
	public class Subscription
	{
        private static readonly log4net.ILog log = log4net.LogManager.GetLogger(System.Reflection.MethodBase.GetCurrentMethod().DeclaringType);

        private IDictionary<String, String> headers;

		/// <summary>
        /// OnMessageHandler
		/// </summary>
		/// <param name="notification">The message received.</param>
		public delegate void OnMessageHandler(NetNotification notification);
		
        /// <summary>
        /// Event fired when a new message, related with "this" subscription is received.
        /// </summary>
		public event OnMessageHandler OnMessage;
		
		#region Private members
		
		private string destinationPattern;
		private NetAction.DestinationType destinationType;

        private bool autoAcknowledge = true;
        private BrokerClient brokerClient;

		#endregion
		
		public Subscription(string destinationPattern, NetAction.DestinationType destinationType)
		{
			this.destinationPattern = destinationPattern;
			this.destinationType = destinationType;
		}
		/// <summary>
		/// A subscription pattern (eg. "/topic/foo", "/topic/.*", "myVirtualQueue@/topic/.*")
		/// </summary>
		public string DestinationPattern {
			get {
				return destinationPattern;
			}
		}

        /// <summary>
        /// Message headers
        /// </summary>
        public IDictionary<String, String> Headers
        {
            get { return headers; }
            set { this.headers = value; }
        }

        /// <summary>
        /// Set a message headers
        /// </summary>
        public void SetHeader(string header, string value)
        {
            if (headers == null)
            {
                Headers = new Dictionary<string, string>();
            }
            this.headers.Add(header, value);
        }

        /// <summary>
        /// Message used to notify clients of a new message received. This is meant to be used by the messaging framework.
        /// </summary>
        /// <param name="notification"></param>
		internal void FireOnMessage(NetNotification notification)
		{
            if (OnMessage != null)
            {
                try
                {
                    OnMessage(notification);
                    if (notification.Headers != null)
                    {
                        if (notification.Headers.ContainsKey("ACK_REQUIRED"))
                        {
                            notification.Headers["ACK_REQUIRED"].ToLower().Equals("false");
                            return;
                        }
                    }
                    if (autoAcknowledge && notification.DestinationType != NetAction.DestinationType.TOPIC)
                    {
                        brokerClient.Acknowledge(notification);
                    }
                }
                catch (Exception e)
                {
                    log.Error("Notification failure", e);
                }
            }
		}

        /// <summary>
        /// Enables Auto Acknowledge on received messages received from queues, that is, a message is automatically acknowledged when successfully processed.
        /// </summary>
        public bool AutoAcknowledge
        {
            get { return autoAcknowledge; }
            set { autoAcknowledge = value; }
        }

        /// <summary>
        /// BrokerClient instance
        /// </summary>
        internal BrokerClient BrokerClient
        {
            get { return brokerClient; }
            set { brokerClient = value; }
        }

        /// <summary>
        /// The destination type (TOPIC, QUEUE or VIRTUA_QUEUE)
        /// </summary>
		public NetAction.DestinationType DestinationType {
			get {
				return destinationType;
			}
		}
        /// <summary>
        /// Get a NetMessage containing a NetSubscribe object that represents this subscription request. This is meant to be used by the messaging framework.
        /// </summary>
        /// <returns>A NetMessage instance.</returns>
        public NetMessage ToNetMessage()
        {
            NetSubscribe netSubscribe = new NetSubscribe(this.destinationPattern, this.destinationType);
            NetAction netAction = new NetAction(NetAction.ActionType.SUBSCRIBE);
            netAction.SubscribeMessage = netSubscribe;
            NetMessage netMessage = new NetMessage(netAction, this.Headers);

            return netMessage;
        }
		
		public override bool Equals (object obj)
		{
			if( ! obj.GetType().Equals(this.GetType()) )
			   return false;
			 
			Subscription other = (Subscription)obj;
			
			if( ! destinationType.Equals(other.destinationType) )
				return false;
			
			if( ! destinationPattern.Equals( other.destinationPattern ) )
				return false;
			
			return true;			
		}
		
		public override int GetHashCode ()
		{
			return destinationType.GetHashCode () ^ destinationPattern.GetHashCode() ;
		}
	}
}
