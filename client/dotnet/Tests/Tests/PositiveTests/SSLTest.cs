using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using SapoBrokerClient;
using System.Security.Cryptography.X509Certificates;

namespace Tests.Tests.PositiveTests
{
    class SSLTest : MultiplePubSubTest
    {
        public SSLTest()
            : base(" N consumer, N producer - SSL positive test ")
        {
        }

        public override void AddProducers()
        {
            base.AddProducers();

            TestClientInfo tci = new TestClientInfo();
            tci.brokerClient = new SslBrokerClient(new HostInfo(TestContext.GetValue("agent1-host"), Int32.Parse(TestContext.GetValue("agent1-ssl-port"))), GetCertCollection());
            //tci.numberOfExecutions = 1;

            base.AddProducersInfo(tci);
        }

        public override void AddConsumers()
        {
            base.AddConsumers();

            TestClientInfo tci = new TestClientInfo();
            tci.brokerClient = new SslBrokerClient(new HostInfo(TestContext.GetValue("agent1-host"), Int32.Parse(TestContext.GetValue("agent1-ssl-port"))), GetCertCollection());

            base.AddConsumerInfo(tci);
        }

        private X509CertificateCollection GetCertCollection()
        {
            X509CertificateCollection certCollection = new X509CertificateCollection();
            string certLocation = TestContext.GetValue("certLocation");
            X509Certificate cert = X509Certificate.CreateFromCertFile(certLocation);
            certCollection.Add(cert);
            return certCollection;
        }
    }
}
