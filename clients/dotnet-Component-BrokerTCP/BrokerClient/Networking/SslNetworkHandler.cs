using System;
using System.IO;
using System.Collections.Generic;
using System.Security.Cryptography.X509Certificates;
using System.Text;
using System.Net.Security;

namespace SapoBrokerClient.Networking
{
    class SslNetworkHandler : NetworkHandler
    {
        private readonly log4net.ILog log = log4net.LogManager.GetLogger(System.Reflection.MethodBase.GetCurrentMethod().DeclaringType);
        private X509CertificateCollection acceptableCertificates;

        public SslNetworkHandler(IList<HostInfo> hosts, X509CertificateCollection acceptableCertificates) : base(hosts)
        {
            this.acceptableCertificates = acceptableCertificates;
        }

        protected override Stream GetCommunicationStream()
        {
            SslStream sslStream = new SslStream(base.GetCommunicationStream(), false, new RemoteCertificateValidationCallback(RemoteCertificateValidation), null);

            sslStream.AuthenticateAsClient("Sapo-Broker");

            return sslStream;
        }

         public bool RemoteCertificateValidation(object sender, X509Certificate certificate, X509Chain chain, SslPolicyErrors sslPolicyErrors)
        {
            if (sslPolicyErrors == SslPolicyErrors.None)
                return true;


            if ( (sslPolicyErrors == SslPolicyErrors.RemoteCertificateChainErrors) && (acceptableCertificates != null))
            {
                foreach (X509Certificate cert in acceptableCertificates)
                {
                    if (cert.Equals(certificate))
                        return true;
                }
            }

            log.Error("Agent's certificate is invalid and there is no equal trusted certificate.");

            // Do not allow this client to communicate with unauthenticated servers.
            return false;
        }
    }
}
