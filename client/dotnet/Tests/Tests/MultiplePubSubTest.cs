using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;

using SapoBrokerClient;
using SapoBrokerClient.Utils;
using Tests.SimpleTestsFramework;

namespace Tests.Tests
{
    public abstract class MultiplePubSubTest : Test
    {
        public class TestClientInfo
        {
            public TestClientInfo()
            {
            }

            public BrokerClient brokerClient;
            public NotificationHandler notificationHandler;
            //public int numberOfExecutions;
        }

        public IList<TestClientInfo> ConsumersInfo {get; set;}
        public IList<TestClientInfo> ProducersInfo {get; set;}

        private string baseName = RandomString.GetRandomString(10);
        public virtual string DestinationName{ get; set;}
        public virtual string SubscriptionName { get; set;}
        public virtual NetAction.DestinationType PublishDestinationType { get; set; }
        public virtual NetAction.DestinationType SubscribeDestinationType { get; set; }
        public virtual byte[] Payload {get; set;}


        private int defaultDataLenght = 10;

        public int DataLength { get; set;}

        public MultiplePubSubTest(string name) : base(name)
        {
            DataLength = defaultDataLenght;

            DestinationName = String.Format(@"/{0}/foo", baseName);
            SubscriptionName = String.Format(@"/{0}/foo", baseName);
            
            PublishDestinationType = SubscribeDestinationType = NetAction.DestinationType.TOPIC;

            Payload = GetData(this.DataLength);

            ConsumersInfo = new List<TestClientInfo>();
            ProducersInfo = new List<TestClientInfo>();
        }


        public override void Build()
        {
            AddProducers();
            AddConsumers();

            AddPreRequisites();
            AddAction();
            AddConsequences();
            AddEpilogues();
        }

        public virtual void AddProducers()
        {
            TestClientInfo tci = new TestClientInfo();
            tci.brokerClient = new BrokerClient(new HostInfo(TestContext.GetValue("agent1-host"), Int32.Parse(TestContext.GetValue("agent1-port"))));
            //tci.numberOfExecutions = 1;

            this.AddProducersInfo(tci);
        }
        public virtual void AddConsumers()
        {
            TestClientInfo tci = new TestClientInfo();

            tci.brokerClient = new BrokerClient(new HostInfo(TestContext.GetValue("agent1-host"), Int32.Parse(TestContext.GetValue("agent1-port"))));
            //tci.numberOfExecutions = ProducersInfo.Count() ;

            this.AddConsumerInfo(tci);
        }


        public virtual void AddPreRequisites()
        {
            PreRequisite preReq  = new PreRequisite("Subscription");
            
            preReq.Runnable = () =>
            {
                foreach (TestClientInfo tci in this.ConsumersInfo)
                {
                    int expectedMessages = 0;
                    if (this.SubscribeDestinationType != NetAction.DestinationType.TOPIC)
                    {
                        if ((ProducersInfo.Count % ConsumersInfo.Count) != 0) throw new Exception(String.Format("When not using TOPIC subscriptions, publishers ({0}) must be multiple of consumers ({1}).", ProducersInfo.Count, ConsumersInfo.Count));
                        expectedMessages = ProducersInfo.Count / ConsumersInfo.Count;
                        Console.WriteLine("In here..");
                    }
                    else
                    {
                        expectedMessages = ProducersInfo.Count;
                    }

                    Console.WriteLine("expectedMessages: {0}", expectedMessages);


                    NotificationHandler notificationHandler = new NotificationHandler(tci.brokerClient, this.SubscribeDestinationType, expectedMessages);
                    tci.notificationHandler = notificationHandler;
                    tci.brokerClient.Subscribe(GetSubscription(notificationHandler));
                }
                preReq.Sucess = true;
                preReq.Done = true;
                Thread.Sleep(1000);// giving time to subscribe. Not critical, so there's no need for something more sophisticated.
            };

            this.AddPrequisite(preReq);
        }

        public virtual void AddAction()
        {
            MainAction action = new MainAction("Publish", "producer");
            action.Runnable = () => {
                foreach(TestClientInfo tci in ProducersInfo)
                {
                    NetBrokerMessage brokerMessage = new NetBrokerMessage( Payload );
                    if( PublishDestinationType.Equals( NetAction.DestinationType.TOPIC ) )
                    {
                        tci.brokerClient.Publish(brokerMessage, this.DestinationName);
                    }
                    else
                    {
                        tci.brokerClient.Enqueue(brokerMessage, this.DestinationName);
                    }
                    tci.brokerClient.Close();
                }
                action.Sucess = true;
                action.Done = true;
            };
            this.SetAction(action);
        }
        public virtual void AddConsequences()
        {
            
            foreach (TestClientInfo tci in ConsumersInfo)
            {
                Consequence consequence = new Consequence("Consume", "Consumer");
                consequence.Runnable = () =>
                {
                    
                    bool signaled = tci.notificationHandler.ManualResetEvent.WaitOne(this.Timeout, false);
                    if (!signaled)
                    {
                        consequence.Sucess = false;
                        consequence.Done = false;
                        return;
                    }

                    foreach (NetNotification notification in tci.notificationHandler.Notifications)
                    {
                        if (!notification.Destination.Equals(this.SubscriptionName))
                        {
                            consequence.ReasonForFailure = String.Format("Unexpected Destination. Expected '{0}', Received: '{1}'", this.SubscriptionName, notification.Destination);
                            return;
                        }
                        //if ( !System.Array.Equals(notification.Message.Payload, Payload ) )
                        if (!EqualArray(notification.Message.Payload,Payload) )
                        {
                            consequence.ReasonForFailure = String.Format("Message Payload was different (content). Expected size '{0}', Received size: '{1}'", this.Payload.Length, notification.Message.Payload.Length);
                            return;
                        }                        
                    }
                    tci.brokerClient.Close();
                    consequence.Sucess = true;
                    consequence.Done = true;
                };
                this.AddConsequence(consequence);
            }
        }

        private static bool EqualArray(byte[] a, byte[] b)
        {
            if (a.Length != b.Length)
                return false;

            for (int i = 0; i != a.Length; ++i)
            {
                if (a[i] != b[i])
                    return false;
            }
            return true;

        }


        public virtual void AddEpilogues()
        {
            //nothing to be done
        }


        public void AddConsumerInfo(TestClientInfo consumerInfo)
        {
            lock(this)ConsumersInfo.Add(consumerInfo);
        }
        public void AddProducersInfo(TestClientInfo producerInfo)
        {
            lock (this) ProducersInfo.Add(producerInfo);
        }

        public virtual string GetBaseName()
        {
            return this.baseName;
        }

        public byte[] GetData(int length)
        {
            return System.Text.Encoding.UTF8.GetBytes(RandomString.GetRandomString(length));
        }

        public virtual Subscription GetSubscription(NotificationHandler notificationHandler)
        {
            Subscription subs = new Subscription(this.SubscriptionName, notificationHandler.ActionType);
            subs.OnMessage += notificationHandler.OnMessageHandler;
            
            return subs;
        }
    }
}
