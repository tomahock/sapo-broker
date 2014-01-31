using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using SapoBrokerClient;
using Samples.Utils;
using RJH.CommandLineHelper;

namespace Samples.Consumers
{
    class BlogConsumer
    {
		volatile static int count = 0;
        public static void Main(string[] args)
        {
            Console.WriteLine("New Blog Consumer");

            BrokerClient brokerClient = new BrokerClient(new HostInfo("broker.bk.sapo.pt", 3323));

            Subscription subscription = new Subscription("test@/sapo/blogs/activity/post", NetAction.DestinationType.VIRTUAL_QUEUE);
            
            subscription.OnMessage += delegate(NetNotification notification)
            {
               if( ( (++count) %100 ) == 0)
				{
					 Console.WriteLine("{0} - New message received. Count: {1}", DateTime.Now.ToLongTimeString(), count );
				}
				brokerClient.Acknowledge(notification.Destination, notification.Message.MessageId);
            };

            brokerClient.Subscribe(subscription);

            Console.WriteLine("Write X to unsbscribe and exit");
            while (!System.Console.Read().Equals('X'))
                ;
            Console.WriteLine();
            Console.WriteLine("Unsubscribe...");
            
            // Note Subscription instance could other than the one used for subscription as long as it was equivelent (same destination type and subscription pattern). Since the application is ending and therefor the socket will be closed agent's will discard the previous subscription. 
            brokerClient.Unsubscribe(subscription);

            Console.WriteLine("Good bye");
        }
    }
}
