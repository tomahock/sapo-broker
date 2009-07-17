using System;
using System.IO;
using System.Net;
using System.Collections.Generic;
using System.Text;
using System.Xml;

namespace SapoBrokerClient.Authentication
{
    public class SapoStsProvider : ICredentialsProvider
    {
        private static readonly log4net.ILog log = log4net.LogManager.GetLogger(System.Reflection.MethodBase.GetCurrentMethod().DeclaringType);

        private string sapoStsLocation = null;
        private static string tokenTTL = "2:00:00";

        #region ICredentialsProvider Members

        public AuthenticationInfo GetCredentials(AuthenticationInfo clientAuthInfo)
        {
            string url = GetUrl(clientAuthInfo);
            string resp = MakeWebRequest(url);

            if (resp == null)
                throw new InvalidCredentialsException();

            AuthenticationInfo newAuthInfo = new AuthenticationInfo(clientAuthInfo.UserId, clientAuthInfo.Roles, System.Text.Encoding.UTF8.GetBytes(resp), clientAuthInfo.UserAuthenticationType);


            return newAuthInfo;
        }

        public bool Init(ProviderInfo info)
        {
            if (info.SpecificData == null)
                throw new ArgumentNullException("SapoStsProvider requires that ProviderInfo.SpecificData contains the location of Sapo STS.");
            
            sapoStsLocation = info.SpecificData as string;
            
            if (info.SpecificData == null)
                throw new Exception("SapoStsProvider requires that ProviderInfo.SpecificData contains a string with the location of Sapo STS.");

            return true;
        }

        public string AuthenticationType
        {
            get { return "SapoSTS"; }
        }

        #endregion

        private string GetUrl(AuthenticationInfo clientAuthInfo)
        {
            string url = String.Format("{0}GetToken?ESBUsername={1}&ESBPassword={2}&ESBTokenTimeToLive={3}", sapoStsLocation, clientAuthInfo.UserId, System.Text.Encoding.UTF8.GetString(clientAuthInfo.Token), tokenTTL);

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
