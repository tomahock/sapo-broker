using System;
using System.Collections.Generic;
using System.Text;

namespace SapoBrokerClient.Utils
{
    public class NotifiableEvent<T>
    {
        public delegate void NotificationHandler(T param);

        private NotificationHandler handler;

        public event NotificationHandler OnEvent
        {
            add
            {
                lock (this)
                {
                    handler += value;
                }
            }
            remove
            {
                lock (this)
                {
                    handler -= value;
                }
            }
        }

        public void Fire(T param)
        {
            NotificationHandler audience = null;

            lock (this)
            {
               audience = handler;
            }

            if (audience != null) audience(param);
        }

    }
}
