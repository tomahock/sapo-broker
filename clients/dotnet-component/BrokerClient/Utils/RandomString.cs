using System;
using System.Collections.Generic;
using System.Text;

namespace SapoBrokerClient.Utils
{
    public static class RandomString
    {
        private static Random random = new Random(/*(int)(DateTime.Now.Ticks % (Math.Pow(2, 32)))*/);

        public static string GetRandomString(int numberOfBytes)
        {
            if (numberOfBytes < 1)
                throw new ArgumentOutOfRangeException("'numberOfBytes' must be greater than 0.");

            StringBuilder sb = new StringBuilder(numberOfBytes);
            lock (random)
            {
                do
                {
                    sb.Append((char)random.Next('a', 'z'+1));
                } while ((--numberOfBytes) != 0);
            }

            return sb.ToString();
        }
    }
}
