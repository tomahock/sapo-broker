using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using SapoBrokerClient;
using Tests.SimpleTestsFramework;

namespace Tests.Tests.PositiveTests
{
    public class PollTest : MultiplePubSubTest
    {
        public PollTest()
            : base("Poll test")
        {
            base.PublishDestinationType = NetAction.DestinationType.QUEUE;
        }
        public override void AddConsumers()
        {
            // no consumers
        }

        public override void AddConsequences()
        {
            Consequence consequence = new Consequence("Poll", "Consumer");
            consequence.Runnable = () =>
            {
                BrokerClient brokerClient = new BrokerClient(new HostInfo(TestContext.GetValue("agent1-host"), Int32.Parse(TestContext.GetValue("agent1-port"))));
                NetNotification notification = brokerClient.Poll(DestinationName, 10000);

                if (notification != null)
                {
                    consequence.Sucess = true;
                    brokerClient.Acknowledge(notification.Subscription, notification.Message.MessageId);
                }

                brokerClient.Close();
                consequence.Done = true;
            };
            this.AddConsequence(consequence);
        }
    }
}
