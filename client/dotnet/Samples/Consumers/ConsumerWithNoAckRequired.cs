using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using SapoBrokerClient;
using Samples.Utils;
using RJH.CommandLineHelper;

namespace Samples.Consumers
{
    class ConsumerWithNoAckRequired
    {
        public static void Main(string[] args)
        {
            Console.WriteLine("ConsumerWithNoAckRequired test");

            if (args.Length == 0)
            {
                System.Console.WriteLine(CommandLineArguments.Usage());
                return;
            }

            CommandLineArguments cliArgs = new CommandLineArguments();
            Parser parser = new Parser(System.Environment.CommandLine, cliArgs);
            parser.Parse();

            BrokerClient brokerClient = new BrokerClient(new HostInfo(cliArgs.Hostname, cliArgs.PortNumber));

            Subscription subscription = new Subscription(cliArgs.DestinationName, cliArgs.DestinationType);

            if (cliArgs.DestinationType != NetAction.DestinationType.TOPIC)
            {
                subscription.SetHeader("ACK_REQUIRED", "false");
            }

            int i = 0;
            subscription.OnMessage += delegate(NetNotification notification)
            {
                System.Console.WriteLine("Message received: {0}, Total: {1}",
                                         System.Text.Encoding.UTF8.GetString(notification.Message.Payload), (++i).ToString());

                IDictionary<string, string> headers = notification.Headers;
                if (headers.Keys != null)
                {
                    System.Console.WriteLine("Headers:");

                    foreach (string header in headers.Keys)
                    {
                        System.Console.WriteLine("{0} - {1}", header, headers[header]);
                    }
                }

                /*
                 *  ACK IS NOT REQUIRED because ACK_REQUIRED was set to false.
                 *  
                if (notification.DestinationType != NetAction.DestinationType.TOPIC)
                {
                    brokerClient.Acknowledge(notification);
                }
                 */
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
