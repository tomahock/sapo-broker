using System;
using System.Collections.Generic;
using System.Text;

namespace SapoBrokerClient.Authentication
{
    public class ProviderInfo
    {
        private readonly string name;
        private readonly object specificData;

        public ProviderInfo(string name, object specificData)
        {
            this.name = name;
            this.specificData = specificData;
        }

        public String Name
        {
            get{ return name; }
        }

        /// <summary>
        /// A reference to an object containing credentials provider specifica data.
        /// </summary>
        /// <returns>An object containing specific data</returns>
        public object SpecificData
        {
            get { return specificData; }
        }
    }
}
