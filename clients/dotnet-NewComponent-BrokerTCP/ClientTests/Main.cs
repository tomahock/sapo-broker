using System;
using System.Collections.Generic;
using System.Threading;
using System.Security.Cryptography.X509Certificates;

using SapoBrokerClient;
using SapoBrokerClient.Authentication;

using RJH.CommandLineHelper;

namespace ClientTests
{
	class MainClass
	{
		public static void Main(string[] args)
		{
            Console.WriteLine(DateTime.Now);
            try
            {
                Console.WriteLine("Testing .NET Broker client!");

               // log4net.Config.BasicConfigurator.Configure();

                Producer(args);
                //UdpProducer(args);
                //Consumer(args);
                Consumer2(args);
                //SslConsumer(args);
                //AuthConsumer(args);
                //Failover(args);
                //Ping(args);
                //Poll(args);

                //QuickBrokerTest.IgorTest.Test(args);
            }
            catch (Exception e)
            {
                Console.WriteLine("It failed... {0}", e);
            }
        }

        #region Poll test

        public static void Poll(string[] args)
        {
            Console.WriteLine("Poll test");

            CommandLineArguments cliArgs = new CommandLineArguments();
            Parser parser = new Parser(System.Environment.CommandLine, cliArgs);
            parser.Parse();

            BrokerClient brokerClient = new BrokerClient(new HostInfo(cliArgs.Hostname, cliArgs.PortNumber));
            try
            {
                NetNotification notification = brokerClient.Poll(@"/ntkclfbiyl/foo", 10000);
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

        #endregion


        #region Ping test

        public static void Ping(string[] args)
        {
            Console.WriteLine("Ping test");

            CommandLineArguments cliArgs = new CommandLineArguments();
            Parser parser = new Parser(System.Environment.CommandLine, cliArgs);
            parser.Parse();

            BrokerClient brokerClient = new BrokerClient(new HostInfo(cliArgs.Hostname, cliArgs.PortNumber));

            NetPong pong = brokerClient.Ping();

            Console.WriteLine("Pong was null? {0}", pong == null);
        }

        #endregion

        #region Consumers

        public static void Consumer(string[] args)
		{
			Console.WriteLine("Consumer test");
			
			BrokerClient brokerClient = new BrokerClient(new HostInfo("lcosta-desktop", 3323));
			
			Subscription subscription = new Subscription(@"/topic/.*", NetAction.DestinationType.TOPIC);
			subscription.OnMessage += (notification) => {
				System.Console.WriteLine( "Message received: {0}", 
				                         System.Text.Encoding.UTF8.GetString ( notification.Message.Payload ) );
                if (notification.DestinationType != NetAction.DestinationType.TOPIC)
                    brokerClient.Acknowledge(notification.Subscription, notification.Message.MessageId);
			};
			
			brokerClient.Subscribe(subscription);
			
			Console.WriteLine("Write X to unsbscribe and exit");
			while(! System.Console.Read().Equals('X') )
				;
			Console.WriteLine();
			Console.WriteLine("Unsubscribe...");


            // Note Subscription instance could other than the one used for subscription as long as it was equivelent (same destination type and subscription pattern). Since the application is ending and therefor the socket will be closed agent's will discard the previous subscription. 
			brokerClient.Unsubscribe(subscription);
			
			
			Console.WriteLine( "Good bye" );			
		}

        static volatile int i = 0;

        public static void Consumer2(string[] args)
        {
            Console.WriteLine("Consumer2 test");

            CommandLineArguments cliArgs = new CommandLineArguments();
            Parser parser = new Parser(System.Environment.CommandLine, cliArgs);
            parser.Parse();

            BrokerClient brokerClient = new BrokerClient(new HostInfo(cliArgs.Hostname, cliArgs.PortNumber));

            Subscription subscription = new Subscription(cliArgs.DestinationName, cliArgs.DestinationType);
            subscription.OnMessage += delegate(NetNotification notification)
            {
                if (notification.DestinationType != NetAction.DestinationType.TOPIC)
                    brokerClient.Acknowledge(notification.Subscription, notification.Message.MessageId);
                System.Console.WriteLine("Message received: {0}, Total: {1}",
                                         System.Text.Encoding.UTF8.GetString(notification.Message.Payload), (++i).ToString());
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

        public static void Failover(string[] args)
        {
            Console.WriteLine("Consumer with failover test");

            CommandLineArguments cliArgs = new CommandLineArguments();
            Parser parser = new Parser(System.Environment.CommandLine, cliArgs);
            parser.Parse();

            List<HostInfo> hosts = new List<HostInfo>();
            hosts.Add(new HostInfo("lcosta-desktop", 3323));
            hosts.Add(new HostInfo("lcosta-desktop", 3423));

            BrokerClient brokerClient = new BrokerClient(hosts);
            Subscription subscription = new Subscription(cliArgs.DestinationName, cliArgs.DestinationType);
            subscription.OnMessage += delegate(NetNotification notification)
            {
                System.Console.WriteLine("Message received: {0}",
                                         System.Text.Encoding.UTF8.GetString(notification.Message.Payload));
                if (notification.DestinationType != NetAction.DestinationType.TOPIC)
                    brokerClient.Acknowledge(notification.Destination, notification.Message.MessageId);
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

        public static void SslConsumer(string[] args)
        {
            Console.WriteLine("SSL Consumer test");

            CommandLineArguments cliArgs = new CommandLineArguments();
            Parser parser = new Parser(System.Environment.CommandLine, cliArgs);
            parser.Parse();

            X509CertificateCollection certCollection = null;
            if (cliArgs.CertificatePath != null)
            {
                X509Certificate cert = X509Certificate.CreateFromCertFile(cliArgs.CertificatePath);
                X509Certificate badCert = X509Certificate.CreateFromCertFile(@"c:\cgd.cer");

                certCollection = new X509CertificateCollection();
                certCollection.Add(badCert);
                certCollection.Add(cert);

            }

            SslBrokerClient brokerClient = new SslBrokerClient(new HostInfo(cliArgs.Hostname, cliArgs.PortNumber), certCollection);

            Subscription subscription = new Subscription(cliArgs.DestinationName, cliArgs.DestinationType);
            subscription.OnMessage += delegate(NetNotification notification)
            {
                System.Console.WriteLine("Message received: {0}",
                                         System.Text.Encoding.UTF8.GetString(notification.Message.Payload));
                if (notification.DestinationType != NetAction.DestinationType.TOPIC)
                    brokerClient.Acknowledge(notification.Subscription, notification.Message.MessageId);
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

        public static void AuthConsumer(string[] args)
        {
            Console.WriteLine("Auth Consumer test");

            CommandLineArguments cliArgs = new CommandLineArguments();
            Parser parser = new Parser(System.Environment.CommandLine, cliArgs);
            parser.Parse();

            X509CertificateCollection certCollection = null;
            if (cliArgs.CertificatePath != null)
            {
                X509Certificate cert = X509Certificate.CreateFromCertFile(cliArgs.CertificatePath);

                certCollection = new X509CertificateCollection();
                certCollection.Add(cert);
            }

            List<HostInfo> hosts = new List<HostInfo>();
            hosts.Add(new HostInfo("lcosta-desktop", 3390));
            hosts.Add(new HostInfo("lcosta-desktop", 3490));

            SslBrokerClient brokerClient = new SslBrokerClient(hosts, certCollection);

            brokerClient.OnFault += (f) =>
            {
                Console.WriteLine("Error");
                Console.WriteLine(String.Format("Code: {0}, Message: {1}, Detail: {2}", f.Code, f.Message, f.Detail) );
            };
            
            SapoStsProvider provider = new SapoStsProvider();
            
            ProviderInfo providerInfo = new ProviderInfo("SapoSTS", "https://services.sapo.pt/sts/");
            provider.Init(providerInfo);
            AuthenticationInfo authInfo = new AuthenticationInfo("bad_username", "bad_password");

            if (!brokerClient.Authenticate(provider, authInfo))
            {
                Console.WriteLine("Authentication failed");
                return;
            }


            Subscription subscription = new Subscription(/*cliArgs.DestinationName*/ "/secret/foo", cliArgs.DestinationType);
            subscription.OnMessage += delegate(NetNotification notification)
            {
                System.Console.WriteLine("Message received: {0}",
                                         System.Text.Encoding.UTF8.GetString(notification.Message.Payload));
                if (notification.DestinationType != NetAction.DestinationType.TOPIC)
                    brokerClient.Acknowledge(notification.Subscription, notification.Message.MessageId);

                //Application should end
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

		#endregion
		
		#region Producer
		public static void Producer(string[] args)
		{
			Console.WriteLine("Producer test");

            CommandLineArguments cliArgs = new CommandLineArguments();
            Parser parser = new Parser(System.Environment.CommandLine, cliArgs);
            parser.Parse();
			
			BrokerClient brokerClient = new BrokerClient(new HostInfo(cliArgs.Hostname, cliArgs.PortNumber));
			
			PublishMessages(brokerClient, cliArgs.DestinationName, 100);
		}
				
		private static void PublishMessages(BrokerClient brokerClient, string destination,int numberOfMessages)
		{
			//string message = "Hello, how are you?";
            int i = 0;
			while( (numberOfMessages--) != 0){
				System.Console.WriteLine("Publishing message");
				NetBrokerMessage brokerMessage = new NetBrokerMessage(System.Text.Encoding.UTF8.GetBytes((i++).ToString()));                                        
				brokerClient.Enqueue(brokerMessage, destination);
				System.Threading.Thread.Sleep(50);
			}
		}

        public static void UdpProducer(string[] args)
        {
            Console.WriteLine("UDP Producer test");

            CommandLineArguments cliArgs = new CommandLineArguments();
            Parser parser = new Parser(System.Environment.CommandLine, cliArgs);
            parser.Parse();

            int numberOfMessages = 10;
            string message = "Hello, how are you?";

            while( (numberOfMessages--) != 0){
				System.Console.WriteLine("Publishing UDP message");
				NetBrokerMessage brokerMessage = new NetBrokerMessage(System.Text.Encoding.UTF8.GetBytes(message));                                        
				BrokerClient.PublishMessageOverUdp(brokerMessage, cliArgs.DestinationName, new HostInfo( cliArgs.Hostname, cliArgs.PortNumber), BrokerClient.DefaultMessageSerializer);
				System.Threading.Thread.Sleep(500);
			}
        }
		#endregion
	}
}