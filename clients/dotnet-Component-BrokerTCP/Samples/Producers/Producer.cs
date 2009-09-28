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

            log4net.Config.BasicConfigurator.Configure();

            CommandLineArguments cliArgs = new CommandLineArguments();
            Parser parser = new Parser(System.Environment.CommandLine, cliArgs);
            parser.Parse();

            BrokerClient brokerClient = new BrokerClient(new HostInfo(cliArgs.Hostname, cliArgs.PortNumber));

            PublishMessages(brokerClient, cliArgs.DestinationName, 100);
        }

        private static void PublishMessages(BrokerClient brokerClient, string destination, int numberOfMessages)
        {
            //string message = "Hello, how are you?";
            int i = 0;
            while ((numberOfMessages--) != 0)
            {
                System.Console.WriteLine("Publishing message");
                NetBrokerMessage brokerMessage = new NetBrokerMessage(System.Text.Encoding.UTF8.GetBytes((i++).ToString()));
                brokerClient.Enqueue(brokerMessage, destination);
                System.Threading.Thread.Sleep(50);
            }
        }
    }
}
