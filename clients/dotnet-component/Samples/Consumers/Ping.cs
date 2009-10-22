using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using SapoBrokerClient;
using Samples.Utils;
using RJH.CommandLineHelper;

namespace Samples.Consumers
{
    class Ping
    {
        public static void Main(string[] args)
        {
            Console.WriteLine("Ping test");

            if (args.Length == 0)
            {
                System.Console.WriteLine(CommandLineArguments.Usage());
                return;
            }

            CommandLineArguments cliArgs = new CommandLineArguments();
            Parser parser = new Parser(System.Environment.CommandLine, cliArgs);
            parser.Parse();

            BrokerClient brokerClient = new BrokerClient(new HostInfo(cliArgs.Hostname, cliArgs.PortNumber));

            NetPong pong = brokerClient.Ping();

            Console.WriteLine("Pong was null? {0}", pong == null);
        }
    }
}
