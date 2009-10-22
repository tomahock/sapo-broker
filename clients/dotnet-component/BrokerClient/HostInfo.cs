
using System;

namespace SapoBrokerClient
{
	public class HostInfo
	{
		
		private string hostname;
		private int port;

		public HostInfo(string hostname, int port)
		{
			this.hostname = hostname;
			this.port = port;
		}

		public string Hostname {
			get {
				return hostname;
			}
		}

		public int Port {
			get {
				return port;
			}
		}

        public override string ToString()
        {
            return String.Format("HostInfo - Hostname: {0}, Port: {1}", hostname, port);
        }
	}
}
