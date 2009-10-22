using System;
using System.Collections.Generic;
using System.Text;

using System.Threading;

namespace SapoBrokerClient.Utils
{
    public class HandoverSyncObject<T> where T: class
    {
        private AutoResetEvent arEvent = new AutoResetEvent(false);
        private T value = null;

        public void Offer(T value)
        {
            this.value = value;
            arEvent.Set();
        }
        
        public T Get()
        {
            arEvent.WaitOne();
            return value;
        }
    }
}
