using System;
using System.Collections.Generic;
using System.Text;

namespace SapoBrokerClient.Authentication
{
    /// <summary>
    /// CredentialsProvider should be implemented by types providing credentials, that is, given a set of user information they obtain other type of authentication information (e.g., transform an username-password into a service token).
    /// </summary>
    public interface ICredentialsProvider
    {
        AuthenticationInfo GetCredentials();

        string AuthenticationType
        {
            get;
        }
    }
}
