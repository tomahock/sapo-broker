using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using SapoBrokerClient;
using Samples.Utils;
using RJH.CommandLineHelper;

namespace Samples.Producers
{
    class Producer
    {
        public static void Main(string[] args)
        {
            Console.WriteLine("Producer test");

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

            PublishMessages(brokerClient, cliArgs.DestinationName, 100, cliArgs.DestinationType);
        }

        private static void PublishMessages(BrokerClient brokerClient, string destination, int numberOfMessages, NetAction.DestinationType destinationType)
        {
            //string message = "Hello, how are you?";
            int i = 0;
            while ((numberOfMessages--) != 0)
            {
                System.Console.WriteLine("Publishing message");
                NetBrokerMessage brokerMessage = new NetBrokerMessage(System.Text.Encoding.UTF8.GetBytes((i++).ToString()));
                if (destinationType == NetAction.DestinationType.TOPIC)
                {
                    brokerClient.Publish(brokerMessage, destination);
                }
                else
                {
                    brokerClient.Enqueue(brokerMessage, destination);
                }
                System.Threading.Thread.Sleep(50);
            }
        }
    }
}
