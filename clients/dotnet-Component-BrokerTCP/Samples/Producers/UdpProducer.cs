using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using SapoBrokerClient;
using Samples.Utils;
using RJH.CommandLineHelper;

namespace Samples.Producers
{
    class UdpProducer
    {
        public static void Main(string[] args)
        {
            Console.WriteLine("UDP Producer test");

            if (args.Length == 0)
            {
                System.Console.WriteLine(CommandLineArguments.Usage());
                return;
            }

            log4net.Config.BasicConfigurator.Configure();

            CommandLineArguments cliArgs = new CommandLineArguments();
            Parser parser = new Parser(System.Environment.CommandLine, cliArgs);
            parser.Parse();

            int numberOfMessages = 10;
            string message = "Hello, how are you?";

            while ((numberOfMessages--) != 0)
            {
                System.Console.WriteLine("Publishing UDP message");
                NetBrokerMessage brokerMessage = new NetBrokerMessage(System.Text.Encoding.UTF8.GetBytes(message));
                BrokerClient.PublishMessageOverUdp(brokerMessage, cliArgs.DestinationName, new HostInfo(cliArgs.Hostname, cliArgs.PortNumber), BrokerClient.DefaultMessageSerializer);
                System.Threading.Thread.Sleep(500);
            }
        }
    }
}
