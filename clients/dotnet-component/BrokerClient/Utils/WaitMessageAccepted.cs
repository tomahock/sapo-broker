using System;
using System.Collections.Generic;
using System.Text;
using System.Threading;

namespace SapoBrokerClient.Utils
{
    public class WaitMessageAccepted : IMessageAcceptedListener
    {

        public enum Result { Accepted, Timeout, Failed };

        public Result WaitResult { get; set; }


        public object SyncObject
        {
            get { return syncObject; }
        }

        private readonly object syncObject;

        public WaitMessageAccepted()
        {
            syncObject = new Object();
        }

        public void MessageAccepted(string ActionId)
        {
            lock (syncObject)
            {
                WaitResult = Result.Accepted;
                Monitor.PulseAll(syncObject);
            }
        }

        public void MessageTimedout(string actionId)
        {
            lock (syncObject)
            {
                WaitResult = Result.Timeout;
                Monitor.PulseAll(syncObject);
            }
        }

        public void MessageFailed(NetFault fault)
        {
            lock (syncObject)
            {
                WaitResult = Result.Failed;
                Monitor.PulseAll(syncObject);
            }
        }
    }
}
