using System;
using System.Security.Cryptography.X509Certificates;
using System.Collections.Generic;
using System.Text;

using SapoBrokerClient.Utils;
using SapoBrokerClient.Encoding;
using SapoBrokerClient.Messaging;
using SapoBrokerClient.Networking;
using SapoBrokerClient.Authentication;


namespace SapoBrokerClient
{
    public class SslBrokerClient : BrokerClient
    {

        #region Private data
        #endregion

        /// <summary>
        /// Creates a SslBrokerClient instance.
        /// </summary>
        /// <param name="hostInfo">Information about an agent.</param>
        public SslBrokerClient(HostInfo hostInfo) : this(hostInfo, null)
		{		
		}
        /// <summary>
        /// Creates a SslBrokerClient instance.
        /// </summary>
        /// <param name="hosts">Information about agents.</param>
        public SslBrokerClient(IList<HostInfo> hosts) : this(hosts, null)
        {
        }
        /// <summary>
        /// Creates a SslBrokerClient instance.
        /// </summary>
        /// <param name="hostInfo">Information about an agent.</param>
        /// <param name="collection">A collection of X.509 certificates. Useful when agents are not using a key that can be validated by a trusted X.509 certificate.</param>
        public SslBrokerClient(HostInfo hostInfo, X509CertificateCollection collection)
        {
            IList<HostInfo> hosts = new List<HostInfo>(1);
            hosts.Add(hostInfo);

            this.hosts = hosts;
            SslNetworkHandler sslNetHandler = new SslNetworkHandler(hosts, collection);
            protocolHandler = new BrokerProtocolHandler(messageSerializer, sslNetHandler);		
        }
        /// <summary>
        /// Creates a SslBrokerClient instance.
        /// </summary>
        /// <param name="hosts">Information about agents.</param>
        /// <param name="collection">A collection of X.509 certificates. Useful when agents are not using a key that can be validated by a trusted X.509 certificate.</param>
        public SslBrokerClient(IList<HostInfo> hosts, X509CertificateCollection collection)
        {
            this.hosts = hosts;
            SslNetworkHandler sslNetHandler = new SslNetworkHandler(hosts, collection);
            protocolHandler = new BrokerProtocolHandler(messageSerializer, sslNetHandler);
        }

        /// <summary>
        /// Client authentication.
        /// </summary>
        /// <param name="clientAuthInfo">Client credentials</param>
        /// <returns>A boolean indicating if the authentication suceeded.</returns>
        public bool Authenticate(ICredentialsProvider provider)
        {
            return protocolHandler.Authenticate(provider);
        }

           
    }
}
