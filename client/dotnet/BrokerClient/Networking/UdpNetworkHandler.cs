using System;
using System.Net;
using System.Net.Sockets;
using System.Collections.Generic;
using System.Text;
using SapoBrokerClient.Encoding;

namespace SapoBrokerClient.Networking
{
    class UdpNetworkHandler
    {
        public static void SendMessage(byte[] data, HostInfo hostInfo, IMessageSerializer messageSerializer)
        {
            //IPHostEntry hostEntry = Dns.GetHostEntry(hostInfo.Hostname);
            //IPEndPoint endPoint = new IPEndPoint(hostEntry.AddressList[0], hostInfo.Port);

            //Socket s = new Socket(endPoint.Address.AddressFamily, SocketType.Dgram, ProtocolType.Udp);
            //s.SendTo(data, endPoint);
            //s.Close();

            short netProtocolType = IPAddress.HostToNetworkOrder(messageSerializer.ProtocolType);
            short netProtocolVersion = IPAddress.HostToNetworkOrder(messageSerializer.ProtocolVersion);
            int netMessageLength = IPAddress.HostToNetworkOrder(data.Length);

            byte[] netProtocolTypeData = BitConverter.GetBytes(netProtocolType);
            byte[] netProtocolVersionData = BitConverter.GetBytes(netProtocolVersion);
            byte[] netMessageLengthData = BitConverter.GetBytes(netMessageLength);

            byte[] mergedData = new byte[netProtocolTypeData.Length + netProtocolVersionData.Length + netMessageLengthData.Length + data.Length];

            // Header
            Array.Copy(netProtocolTypeData, 0, mergedData, 0, netProtocolTypeData.Length);
            Array.Copy(netProtocolVersionData, 0, mergedData, netProtocolTypeData.Length, netProtocolVersionData.Length);
            Array.Copy(netMessageLengthData, 0, mergedData, netProtocolTypeData.Length + netProtocolVersionData.Length, netMessageLengthData.Length);
            // Data
            Array.Copy(data, 0, mergedData, netProtocolTypeData.Length + netProtocolVersionData.Length + netMessageLengthData.Length, data.Length);

            UdpClient client = new UdpClient();
            client.Send(mergedData, mergedData.Length, hostInfo.Hostname, hostInfo.Port);
            client.Close();
        }
    }
}
