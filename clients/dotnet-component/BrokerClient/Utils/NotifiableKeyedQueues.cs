using System;
using System.Threading;
using System.Collections.Generic;
using System.Text;


namespace SapoBrokerClient.Utils
{
    /// <summary>
    /// NotifiableKeyedQueues contains queues identified by a key.
    /// It provides a synchronization mechanism through witch queue consumers are notified of available objects. After a client is notified of an object offer it is re-added to the end of the waiting queue, except if it declares that doesn't want to receive more than one object. If it doesn't want to be notified of more objects it should declare by setting StillInterested property to false.
    /// Clients offer items to a specific queue through Put method. If there aren't interested consumers the method returns false.
    /// Clients Register themselves in order to be notified of new object available to them through a SynchronizationEntry instance.
    /// Queues are automatically created and deleted.
    /// </summary>
    public class NotifiableKeyedQueues<T>
    {
        /// <summary>
        /// SynchronizationEntry objects are used by clients to synchronize themselves (SynchronizationEntry property), being notified when a new objects is available to them (Values property). This is a list because several objects may be offered before the client thread executes.
        /// If clients lose interested in being notified they should declare it by setting StillInterested property to false. 
        /// </summary>
        public class SynchronizationEntry
        {
            private bool receiveMultiple;

            public bool ReceiveMultiple
            {
                get { return receiveMultiple; }
            }

            private object synchronizationObject;

            public object SynchronizationObject
            {
                get { return synchronizationObject; }
                set { synchronizationObject = value; }
            }
            private IList<T> values;

            public IList<T> Values
            {
                get { return this.values; }
            }

            private int stillInterested = 1;

            public bool StillInterested
            {
                get { return stillInterested == 1; }
                set { Interlocked.Exchange(ref stillInterested, (value ? 1 : 0)); }
            }

            public SynchronizationEntry(object synchronizationObject, bool receiveMultiple)
                : this(synchronizationObject, receiveMultiple, new List<T>())
            {
            }
            public SynchronizationEntry(object synchronizationObject, bool receiveMultiple, IList<T> values)
            {
                this.synchronizationObject = synchronizationObject;
                this.receiveMultiple = receiveMultiple;
                this.values = values;
            }
        }

        private static IDictionary<string, Queue<SynchronizationEntry>> consumers = new Dictionary<string, Queue<SynchronizationEntry>>();

        /// <summary>
        /// Registration method through witch clients show interest in being notified when an object available for them in a queue identified by a given key.
        /// </summary>
        /// <param name="key">Key object</param>
        /// <param name="entry">A SynchronizationEntry object</param>
        public static void Register(string key, SynchronizationEntry entry)
        {
            if ((key == null) || (entry == null))
                throw new ArgumentNullException("None of Regist method argumets can be null.");

            if (entry.SynchronizationObject == null)
                throw new ArgumentNullException("SynchronizationEntry<T>.SynchronizationObject cannot be null.");
            Queue<SynchronizationEntry> queue = GetQueue(key);

            lock (queue)
            {
                queue.Enqueue(entry);
            }
        }

        /// <summary>
        /// Offer a object to a given queue
        /// </summary>
        /// <param name="key">Key that identifies the queue.</param>
        /// <param name="value">Value to enqueue</param>
        /// <returns>returs true if the object was accepted (there was a registered client for the queue) or false otherwise.</returns>
        public static bool Offer(string key, T value)
        {
            Queue<SynchronizationEntry> queue = GetQueue(key);
            SynchronizationEntry entry = null;
            lock (queue)
            {
                if (queue.Count == 0)
                {
                    return false;
                }
                else
                {
                    entry = Dequeue(queue, key);
                }
            }
            if (entry.StillInterested)
            {
                lock (entry.Values)
                {
                    entry.Values.Add(value);
                }
                lock (entry.SynchronizationObject)
                {
                    Monitor.Pulse(entry.SynchronizationObject);
                }

                if (entry.ReceiveMultiple)
                {
                    // add client again
                    Register(key, entry);
                }
                return true;
            }
            // Retrieved client wasn't interested. Offer it to another client.
            return Offer(key, value);
        }

        private static Queue<SynchronizationEntry> GetQueue(string key)
        {
            Queue<SynchronizationEntry> queue;
            lock (consumers)
            {
                if (consumers.ContainsKey(key))
                {
                    queue = consumers[key];
                }
                else
                {
                    queue = new Queue<NotifiableKeyedQueues<T>.SynchronizationEntry>();
                    consumers.Add(key, queue);
                }
            }
            return queue;
        }

        private static SynchronizationEntry Dequeue(Queue<SynchronizationEntry> queue, string key)
        {
            SynchronizationEntry entry;
            lock (queue)
            {
                entry = queue.Dequeue();
                if (queue.Count == 0)
                {
                    lock (consumers)
                    {
                        consumers.Remove(key);
                    }
                }
            }
            return entry;
        }

    }
}
