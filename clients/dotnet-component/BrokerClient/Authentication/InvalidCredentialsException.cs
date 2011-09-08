using System;
using System.Collections.Generic;
using System.Text;

namespace SapoBrokerClient.Authentication
{
    public class InvalidCredentialsException : Exception
    {
        public InvalidCredentialsException() { }
        public InvalidCredentialsException(string message) : base(message) { }
    }
}
