using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;

using SapoBrokerClient;

namespace Tests.Tests
{
    public class NotificationHandler
    {
        private BrokerClient client;
        private NetAction.DestinationType actionType;

        public NetAction.DestinationType ActionType
        {
            get { return actionType; }
            set { actionType = value; }
        }
        private int expectedMessages;
        public IList<NetNotification> Notifications {get; set;}
        public ManualResetEvent ManualResetEvent {get; set;}

        public NotificationHandler(BrokerClient client, NetAction.DestinationType actionType, int expectedMessages)
        {
            this.client = client;
            this.actionType = actionType;
            this.expectedMessages = expectedMessages;
            this.Notifications = new List<NetNotification>();
            this.ManualResetEvent = new ManualResetEvent(false);
        }

        public void OnMessageHandler(NetNotification notification)
        {
            lock(this)
            {
                Notifications.Add(notification);

                if(Notifications.Count == expectedMessages )
                {
                    ManualResetEvent.Set();
                }
            }
            if( ! this.actionType.Equals( NetAction.DestinationType.TOPIC ) )
            {
                client.Acknowledge(notification.Subscription, notification.Message.MessageId);
                Console.WriteLine("Acknowledge");
            }
        }
    }
}
