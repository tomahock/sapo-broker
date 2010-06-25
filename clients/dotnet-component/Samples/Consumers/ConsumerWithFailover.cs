using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using SapoBrokerClient;
using Samples.Utils;
using RJH.CommandLineHelper;

namespace Samples.Consumers
{
    class ConsumerWithFailover
    {
        public static void Main(string[] args)
        {
            Console.WriteLine("Consumer with failover test");

            if (args.Length == 0)
            {
                System.Console.WriteLine(CommandLineArguments.Usage());
                return;
            }

            CommandLineArguments cliArgs = new CommandLineArguments();
            Parser parser = new Parser(System.Environment.CommandLine, cliArgs);
            parser.Parse();

            List<HostInfo> hosts = new List<HostInfo>();
            hosts.Add(new HostInfo(cliArgs.Hostname, cliArgs.PortNumber));
            //hosts.Add(new HostInfo(cliArgs.Hostname, 3423)); // Add an alternative

            BrokerClient brokerClient = new BrokerClient(hosts);
            Subscription subscription = new Subscription(cliArgs.DestinationName, cliArgs.DestinationType);
            subscription.OnMessage += delegate(NetNotification notification)
            {
                System.Console.WriteLine("Message received: {0}",
                                         System.Text.Encoding.UTF8.GetString(notification.Message.Payload));
                if (notification.DestinationType != NetAction.DestinationType.TOPIC)
		        {
                    brokerClient.Acknowledge(notification);
		        }
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
