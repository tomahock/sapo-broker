using System;
using System.Collections.Generic;
using System.Text;

namespace SapoBrokerClient.Authentication
{
    /// <summary>
    /// AuthInfo represents client's authentication information.
    /// </summary>
    public class AuthenticationInfo
    {
        private string userId;

        public string UserId
        {
            get { return userId; }
            set { userId = value; }
        }
        private IList<string> roles;

        public IList<string> Roles
        {
            get { return roles; }
            set { roles = value; }
        }
        private byte[] token;

        public byte[] Token
        {
            get { return token; }
            set { token = value; }
        }
        private string userAuthenticationType;

        public string UserAuthenticationType
        {
            get { return userAuthenticationType; }
            set { userAuthenticationType = value; }
        }

        private ICredentialsProvider providerInfo;

        public ICredentialsProvider ProviderInfo
        {
            get { return providerInfo; }
            set { providerInfo = value; }
        }


        /// <summary>
        /// Creates an AuthInfo instance.
        /// </summary>
        /// <param name="userId">User identification, such as an username.</param>
        /// <param name="password">User password. This is transformed in a binary token using UTF-8.</param>
        public AuthenticationInfo(string userId, string password) : this(userId, null, System.Text.Encoding.UTF8.GetBytes(password), null, null) { }

        /// <summary>
        /// Creates an AuthInfo instance.
        /// </summary>
        /// <param name="userId">User identification, such as an username.</param>
        /// <param name="password">User password. This is transformed in a binary token using UTF-8.</param>
        /// <param name="providerInfo">Provider info.</param>
        public AuthenticationInfo(string userId, string password, ICredentialsProvider providerInfo) : this(userId, null, System.Text.Encoding.UTF8.GetBytes(password), null, providerInfo) { }

        /// <summary>
        /// Creates an AuthInfo instance.
        /// </summary>
        /// <param name="userId">User identification, such as an username.</param>
        /// <param name="roles">User roles associated with the roles.</param>
        /// <param name="token">User binary authentication token.</param>
        /// <param name="userAuthenticationType">The type of authentication being used (e.g., SapoSTS).</param>
        /// <param name="providerInfo">Provider info.</param>
        public AuthenticationInfo(String userId, IList<String> roles, byte[] token, String userAuthenticationType, ICredentialsProvider providerInfo)
        {
            this.userId = userId;
            this.roles = roles;
            this.token = token;
            this.userAuthenticationType = userAuthenticationType;
            this.providerInfo = providerInfo;

            if (providerInfo != null)
            {
                this.userAuthenticationType = providerInfo.AuthenticationType;
            }
        }
    }
}
