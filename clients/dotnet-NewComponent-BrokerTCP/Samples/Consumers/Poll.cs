using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using SapoBrokerClient;
using Samples.Utils;
using RJH.CommandLineHelper;

namespace Samples.Consumers
{
    class Poll
    {
        public static void Main(string[] args)
        {
            Console.WriteLine("Poll test");

            log4net.Config.BasicConfigurator.Configure();

            CommandLineArguments cliArgs = new CommandLineArguments();
            Parser parser = new Parser(System.Environment.CommandLine, cliArgs);
            parser.Parse();

            BrokerClient brokerClient = new BrokerClient(new HostInfo(cliArgs.Hostname, cliArgs.PortNumber));
            try
            {
                NetNotification notification = brokerClient.Poll(cliArgs.DestinationName, 10000);
                if (notification != null)
                {
                    System.Console.WriteLine("Message received: {0}",
                                                 System.Text.Encoding.UTF8.GetString(notification.Message.Payload));
                    brokerClient.Acknowledge(notification.Destination, notification.Message.MessageId);
                }
                else
                {
                    Console.WriteLine("Message not received.");
                }
            }
            catch (TimeoutException te)
            {
                Console.WriteLine("Message timedout...", te);
            }
        }
    }
}
