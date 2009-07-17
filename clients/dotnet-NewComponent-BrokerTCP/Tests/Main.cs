using System;
using System.IO;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Xml.Serialization;

using Tests.SimpleTestsFramework;
using SapoBrokerClient.TestParams;

using Tests.Tests.PositiveTests;

namespace Tests
{
    class Program
    {
        static void Main(string[] args)
        {
            TestContext.Init(@"X:\BrokerRepo\clients\dotnet-NewComponent-BrokerTCP\Tests\Tests\TestParms\TestParams.xml");
			//TestContext.Init(@"/home/lcosta/Work/BrokerRepo/clients/dotnet-NewComponent-BrokerTCP/Tests/Tests/TestParms/TestParams.xml");
                        
            int numberOfRuns = Int32.Parse ( TestContext.GetValue("runs") );

            //new C1P1Test().Run(numberOfRuns);
            //new CNPNTest().Run(numberOfRuns);
            //new CNPN_Queue().Run(numberOfRuns);
            new PollTest().Run(numberOfRuns);
            //new SSLTest().Run(numberOfRuns);

            Console.Read();
        }
    }
}
