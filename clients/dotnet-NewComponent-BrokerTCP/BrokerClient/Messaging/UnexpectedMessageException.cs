using System;
using System.Collections.Generic;
using System.Text;

namespace SapoBrokerClient.Messaging
{
    class UnexpectedMessageException : Exception
    {
        private NetMessage netMessage;
        public UnexpectedMessageException(string message, NetMessage netMessage)
            : base(message)
        {
            this.netMessage = netMessage;
        }

        public NetMessage NetMessage
        {
            get { return netMessage; }
        }

    }
}
