using System;

using System.Collections.Generic;

using System.Text;

using SapoBrokerClient;



namespace QuickBrokerTest
{

    public class IgorTest
    {

        private static BrokerClient c = new BrokerClient(new HostInfo("lcosta-desktop", 3323));

        
        public static void Test(string[] args)
        {

            Subscription s = new Subscription("c@cTest", NetAction.DestinationType.VIRTUAL_QUEUE);

            s.OnMessage += OnMessageTest;

            c.Subscribe(s);

            Console.WriteLine("Iniciar Teste Broker V3 - SUB");

            Console.ReadKey();

        }
        
        private static void OnMessageTest(NetNotification notification)
        {
            c.Acknowledge(notification.Subscription, notification.Message.MessageId);

            Console.WriteLine("Nova Mensagem - " + Encoding.UTF8.GetString(notification.Message.Payload));

        }

    }

}
