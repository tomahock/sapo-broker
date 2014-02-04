using System;
using System.IO;
using System.Net;
using System.Collections.Generic;
using System.Text;
using System.Xml;

using SapoBrokerClient.Authentication;

namespace SapoBrokerClient.Authentication.SapoSTS
{
    public class SapoStsProvider : ICredentialsProvider
    {
        private static readonly log4net.ILog log = log4net.LogManager.GetLogger(System.Reflection.MethodBase.GetCurrentMethod().DeclaringType);

        private static string DefaultStsLoaation = "https://services.sapo.pt/sts/";
        private static string tokenTTL = "2:00:00";
        private readonly static string Name = "SapoSTS";

        private string sapoStsLocation;
        private string username;
        private string password;


        public SapoStsProvider(string username, string password) : this(username, password, DefaultStsLoaation) { }
   
        public SapoStsProvider(string username, string password, string stsLocation)
        {
            this.username = username;
            this.password = password;
            this.sapoStsLocation = stsLocation;
        }

        #region ICredentialsProvider Members

        public AuthenticationInfo GetCredentials()
        {
            string url = GetUrl(username, password);
            string resp = MakeWebRequest(url);

            if (resp == null)
                throw new InvalidCredentialsException();

            AuthenticationInfo newAuthInfo = new AuthenticationInfo(username, null, System.Text.Encoding.UTF8.GetBytes(resp), Name);
            
            return newAuthInfo;
        }

        public string AuthenticationType
        {
            get { return Name; }
        }

        #endregion

        private string GetUrl(string username, string password)
        {
            string url = String.Format("{0}GetToken?ESBUsername={1}&ESBPassword={2}&ESBTokenTimeToLive={3}", sapoStsLocation, username, password, tokenTTL);

            return url;
        }

        public string Test { get; set; }

        private string MakeWebRequest(string url)
        {
            System.Net.WebRequest webReq = System.Net.WebRequest.Create(url);
            webReq.Timeout = 5000; //5 seconds
            System.Net.WebResponse webResp = null;
            try
            {
                webResp = webReq.GetResponse();
            }
            catch (System.Net.WebException we)
            {
                HttpWebResponse response = we.Response as HttpWebResponse;

                if (response == null)
                {
                    log.Error("Failed to get User credentials. STS location: " + this.sapoStsLocation, we);
                    throw we;
                }

                Stream errorStream = response.GetResponseStream();

                if (response.StatusCode != HttpStatusCode.InternalServerError)
                {
                    log.Error("Unexpected status code: " + response.StatusCode, we);
                    throw we;
                }

                XmlDocument doc = new System.Xml.XmlDocument();
                doc.Load(errorStream);
                
                XmlNamespaceManager xnm = new XmlNamespaceManager(doc.NameTable);
                xnm.AddNamespace("tns", "http://services.sapo.pt/exceptions");

                XmlNode node = doc.SelectSingleNode("/fault/detail/tns:exceptionInfo/tns:code", xnm);

                if (node == null)
                {
                    log.Error("Unexpected reponse: " + doc.InnerXml, we);
                    return null; 
                }

                if (node.InnerText.Equals("1010"))
                {
                    log.Warn("Invadid credentials");
                    throw new InvalidCredentialsException();
                }
                
                throw we;
            }

            XmlDocument xmlResp = new System.Xml.XmlDocument();
            xmlResp.Load(webResp.GetResponseStream());

            XmlNamespaceManager responseXnm = new XmlNamespaceManager(xmlResp.NameTable);
            responseXnm.AddNamespace("ns", "http://services.sapo.pt/definitions");

            XmlNode tokenNode = xmlResp.SelectSingleNode("/ns:ESBToken", responseXnm);
            return tokenNode.InnerText;
        }
    }
}
