using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using SapoBrokerClient;
using Samples.Utils;
using RJH.CommandLineHelper;
using System.Threading;

namespace Samples.Consumers
{
    class IntensivePolling
    {
        static volatile int messagesReceived = 0;
        static volatile int threadsEnded = 0;

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

            int MAX_CONSUMERS = 32;
            int MESSAGES_TO_RECEIVE = 1000;
            Thread[] consumerThreads = new Thread[MAX_CONSUMERS];

            Object syncObject = new Object();
            
            for (int i = 0; i != MAX_CONSUMERS; ++i)
            {
                Thread t = new Thread(new ThreadStart(() =>
                {
                    Console.WriteLine("Thread started");
                    
                    BrokerClient brokerClient = new BrokerClient(new HostInfo(cliArgs.Hostname, cliArgs.PortNumber));

                    while (true)
                    {
                        try
                        {
                            NetNotification notification = brokerClient.Poll(cliArgs.DestinationName, -1);
                            if (notification != null)
                            {
                                brokerClient.Acknowledge(notification);
                                int count = ++messagesReceived;
                                if (count == MESSAGES_TO_RECEIVE)
                                {
                                    Console.WriteLine("All messages received");

                                    break;
                                }
                            }
                            else
                            {
                                Console.WriteLine("No message received");
                                break;
                            }
                        }
                        catch (TimeoutException te)
                        {
                            Console.WriteLine("Message timedout...", te);
                            break;
                        }
                    }

                    if ((++threadsEnded) == MAX_CONSUMERS)
                    {
                        lock (syncObject)
                        {
                            Monitor.PulseAll(syncObject);
                        }
                    }
                }));
                consumerThreads[i] = t;
                t.Start();
            }
            lock (syncObject)
            {
                Monitor.Wait(syncObject);
            }
            Console.WriteLine("All threads ended.");
        }
    }
}
