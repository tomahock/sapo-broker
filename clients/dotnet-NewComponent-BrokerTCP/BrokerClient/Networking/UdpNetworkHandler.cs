using System;
using System.Net;
using System.Net.Sockets;
using System.Collections.Generic;
using System.Text;

namespace SapoBrokerClient.Networking
{
    class UdpNetworkHandler
    {
        public static void SendMessage(byte[] data, HostInfo hostInfo)
        {
            //IPHostEntry hostEntry = Dns.GetHostEntry(hostInfo.Hostname);
            //IPEndPoint endPoint = new IPEndPoint(hostEntry.AddressList[0], hostInfo.Port);

            //Socket s = new Socket(endPoint.Address.AddressFamily, SocketType.Dgram, ProtocolType.Udp);
            //s.SendTo(data, endPoint);
            //s.Close();

            UdpClient client = new UdpClient();
            client.Send(data, data.Length, hostInfo.Hostname, hostInfo.Port);
            client.Close();
        }
    }
}
