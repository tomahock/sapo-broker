using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;

using SapoBrokerClient;
using Samples.Utils;
using RJH.CommandLineHelper;

namespace Samples.Producers
{
    class MultipleProducers
    {

        private static readonly int NR_THREADS = 8;

        public static void Main(string[] args)
        {
            Console.WriteLine("MultipleProducers test");

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
            int numberOfMessages = 500000;

            Thread[] threads = new System.Threading.Thread[NR_THREADS];

            for (int i = 0; i != threads.Length; ++i)
            {
                threads[i] = new Thread(
                    new ThreadStart(() =>
                    {
                        int msgs = numberOfMessages;
                        int threadId = i;

                        string message = threadId + " - Message";

                        NetBrokerMessage brokerMessage = new NetBrokerMessage(System.Text.Encoding.UTF8.GetBytes(message));

                        while ((--msgs) != 0)
                        {
                            System.Console.WriteLine(message);
                            brokerClient.Publish(brokerMessage, cliArgs.DestinationName);
                        }
                    }
                )
                );

                threads[i].Start();

            }

            Console.WriteLine("Write X to unsbscribe and exit");
            while (!System.Console.Read().Equals('X'))
                ;
        }
    }
}
