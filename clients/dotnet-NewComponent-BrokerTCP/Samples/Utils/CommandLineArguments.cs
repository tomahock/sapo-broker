using System;
using System.Collections.Generic;
using System.Text;

using RJH.CommandLineHelper;
using SapoBrokerClient;

namespace Samples.Utils
{
    class CommandLineArguments
    {
        private int portNumber = 3323;
        private int sslPortNumber = 3390;
        private string hostname = "localhost";
        private string certPath = null;
        private NetAction.DestinationType destinationType = NetAction.DestinationType.TOPIC;
        private string destinationName = @"/topic/foo";

        [CommandLineSwitch("DestinationName", "Destination name (e.g., /topic/.*)")]
        [CommandLineAlias("dn")]
        public string DestinationName
        {
            get { return destinationName; }
            set { destinationName = value; }
        }

        [CommandLineSwitch("Hostname", "Host name")]
        [CommandLineAlias("hn")]
        public string Hostname
        {
            get { return hostname; }
            set { hostname = value; }
        }

        [CommandLineSwitch("PortNumber", "Port number")]
        [CommandLineAlias("port")]
        public int PortNumber
        {
            get { return portNumber; }
            set { portNumber = value; }
        }

        [CommandLineSwitch("SslPortNumber", "SSL Port number")]
        [CommandLineAlias("sslport")]
        public int SslPortNumber
        {
            get { return sslPortNumber; }
            set { sslPortNumber = value; }
        }

        [CommandLineSwitch("DestType", "Destination type (TOPIC, QUEUE, VIRTUAL_QUEUE)")]
        [CommandLineAlias("dt")]
        public NetAction.DestinationType DestinationType
        {
            get { return destinationType; }
            set { destinationType = value; }
        }

        [CommandLineSwitch("CertificatePath", "X.509 certificate path")]
        [CommandLineAlias("cer")]
        public string CertificatePath
        {
            get { return certPath; }
            set { certPath = value; }
        }
    }
}
