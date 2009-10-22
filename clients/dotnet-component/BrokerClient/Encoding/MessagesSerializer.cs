
using System;


namespace SapoBrokerClient.Encoding
{
	public interface IMessageSerializer
	{
		NetMessage Unmarshall(byte[] data);
		byte[] Marshall(NetMessage message);
		
		short ProtocolType{
			get;
		}
		
		short ProtocolVersion{
			get;
		}
	}
}
