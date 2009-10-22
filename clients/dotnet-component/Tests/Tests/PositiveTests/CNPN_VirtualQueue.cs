using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using SapoBrokerClient;

namespace Tests.Tests.PositiveTests
{
    class CNPN_VirtualQueue : MultiplePubSubTest
    {
        public CNPN_VirtualQueue()
            : base(" consumer, N producer - positive test (VirtualQueue Recipient)")
        {
            base.PublishDestinationType = NetAction.DestinationType.TOPIC;
            base.SubscribeDestinationType = NetAction.DestinationType.QUEUE;

            base.SubscriptionName = String.Format("myQueue@{0}", base.SubscriptionName);
        }

        public override void AddProducers()
        {
            base.AddProducers();

            TestClientInfo tci = new TestClientInfo();
            tci.brokerClient = new BrokerClient(new HostInfo(TestContext.GetValue("agent1-host"), Int32.Parse(TestContext.GetValue("agent1-port"))));
            //tci.numberOfExecutions = 1;

            base.AddProducersInfo(tci);
        }

        public override void AddConsumers()
        {
            base.AddConsumers();

            TestClientInfo tci = new TestClientInfo();
            tci.brokerClient = new BrokerClient(new HostInfo(TestContext.GetValue("agent1-host"), Int32.Parse(TestContext.GetValue("agent1-port"))));
            //tci.numberOfExecutions = ProducersInfo.Count();

            base.AddConsumerInfo(tci);
        }

    }
}
