using System;
using System.Collections.Generic;
using System.Text;

namespace SapoBrokerClient.Utils
{
    public class CircularContainer<T> where T : class
    {
        private IList<T> innerContainer;
        private int index = -1;

        public CircularContainer()
        {
            innerContainer = new List<T>();
        }

        public CircularContainer(int capacity)
        {
            innerContainer = new List<T>(capacity);
        }

        public CircularContainer(IList<T> elements)
        {
            innerContainer = new List<T>(elements);
        }

        public void Add(T value)
        {
            lock (innerContainer)
            {
                innerContainer.Add(value);
            }
        }

        public void Remove(T value)
        {
            lock (innerContainer)
            {
                innerContainer.Remove(value);
            }
        }

        public void Clear()
        {
            lock (innerContainer)
            {
                index = -1;
                innerContainer.Clear();
            }
        }

        public int Size()
        {
            lock (innerContainer)
            {
                return innerContainer.Count;
            }
        }

        /**
         * Obtains the current value. 
         * @return a T value.
         */
        public T Peek()
        {
            lock (innerContainer)
            {
                if (index == -1)
                    return null;

                if (innerContainer.Count == 0)
                    return null;

                if (index >= innerContainer.Count)
                    index = 0;

                return innerContainer[index];
            }
        }

        /**
         * Adds the indexer, moving it to the next position (or beginning).
         * @return a T value.
         */
        public T Get()
        {
            lock (innerContainer)
            {
                if (innerContainer.Count == 0)
                    return null;

                if ((++index) >= innerContainer.Count)
                    index = 0;
                return innerContainer[index];
            }
        }
    }
}
