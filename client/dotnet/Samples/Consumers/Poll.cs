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

            if (args.Length == 0)
            {
                System.Console.WriteLine(CommandLineArguments.Usage());
                return;
            }

            log4net.Config.BasicConfigurator.Configure();

            CommandLineArguments cliArgs = new CommandLineArguments();
            Parser parser = new Parser(System.Environment.CommandLine, cliArgs);
            parser.Parse();

            BrokerClient brokerClient = new BrokerClient(new HostInfo(cliArgs.Hostname, cliArgs.PortNumber));
            while (true)
            {
                try
                {
                    //NetNotification notification = brokerClient.Poll(cliArgs.DestinationName); // Wait forever
                    //NetNotification notification = brokerClient.Poll(cliArgs.DestinationName, 0, 30000, null); //Wait forever, Reserve time: 30s, No Accept Request.
                    NetNotification notification = brokerClient.Poll(cliArgs.DestinationName, 2000);
                    if (notification != null)
                    {
                        System.Console.WriteLine("Message received: {0}",
                                                     System.Text.Encoding.UTF8.GetString(notification.Message.Payload));
                        brokerClient.Acknowledge(notification.Destination, notification.Message.MessageId);
                    }
                    else
                    {
                        Console.WriteLine("No message received");
                    }
                }
                catch (TimeoutException te)
                {
                    Console.WriteLine("Message timedout...", te);
                }
            }
        }
    }
}
