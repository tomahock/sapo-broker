using System;
using System.IO;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Xml.Serialization;

using SapoBrokerClient.TestParams;

namespace Tests
{
    public class TestContext
    {
        private static TestParams TestParams { get; set; }

        public static void Init(string parametersFile)
        {
            TextReader reader = new StreamReader(parametersFile);

            XmlSerializer serializer = new XmlSerializer(typeof(TestParams));
            TestParams = (TestParams)serializer.Deserialize(reader);
        }

        public static string GetValue(string testName, string key)
        {
            if (TestParams == null)
                return null;

            var result = from test in TestParams.Tests
                            where test.Name.Equals(testName)
                            from param in test.Param
                            where param.Name.Equals(key)
                            select param.Value;
            
            if (result.Count() != 1) return null;
            
            return result.ElementAt(0);
        }

        public static string GetValue(string key)
        {
            if (TestParams == null)
                return null;

            var result = from param in TestParams.Defaults
                         where param.Name.Equals(key)
                         select param.Value;

            if (result.Count() != 1) return null;
            
            return result.ElementAt(0);
        }

    }
}
