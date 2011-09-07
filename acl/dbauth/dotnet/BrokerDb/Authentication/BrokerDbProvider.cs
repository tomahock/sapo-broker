using System;
using System.IO;
using System.Net;
using System.Collections.Generic;
using System.Text;
using System.Xml;

using SapoBrokerClient.Authentication;

namespace SapoBrokerClient.Authentication.BrokerDb
{
    public class BrokerDbProvider : ICredentialsProvider
    {
        private static readonly log4net.ILog log = log4net.LogManager.GetLogger(System.Reflection.MethodBase.GetCurrentMethod().DeclaringType);

        private readonly static string Name = "BrokerRolesDB";

        private string username;
        private string password;

        public BrokerDbProvider(string username, string password)
        {
            this.username = username;
            this.password = password;
        }

        #region ICredentialsProvider Members

        public AuthenticationInfo GetCredentials()
        {
           AuthenticationInfo newAuthInfo = new AuthenticationInfo(username, null, System.Text.Encoding.UTF8.GetBytes(password), Name);
            
            return newAuthInfo;
        }

        public string AuthenticationType
        {
            get { return Name; }
        }

        #endregion
    }
}
