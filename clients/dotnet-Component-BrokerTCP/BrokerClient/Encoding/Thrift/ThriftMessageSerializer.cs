using System;
using System.IO;

using Thrift.Protocol;
using Thrift.Transport;

using SapoBrokerClient.Encoding;

using SapoBrokerClient.Encoding.Thrift.Messages;

namespace SapoBrokerClient.Encoding.Thrift
{
	public class ThriftMessageSerializer : IMessageSerializer
	{
		private static readonly log4net.ILog log = log4net.LogManager.GetLogger(System.Reflection.MethodBase.GetCurrentMethod().DeclaringType);
		
		public NetMessage Unmarshall(byte[] data)
		{
			try{
				Stream stream = new MemoryStream(data);
				TProtocol tProtocol = new TBinaryProtocol(new TStreamTransport(stream, stream));
	 
				Atom message = new Atom();
				message.Read(tProtocol);
				
				NetMessage netMsg = ThriftMessageConverter.translate(message);
				return netMsg;
			}catch(Exception ex){
				log.Error("Error unmarshalling message", ex);
			}
			return null;
		}
		
		public byte[] Marshall(NetMessage message)
		{
			try{
				Atom thriftMessage = ThriftMessageConverter.translate(message);
				
				MemoryStream stream = new MemoryStream();
				TProtocol tProtocol = new TBinaryProtocol(new TStreamTransport(stream, stream));
 
				thriftMessage.Write(tProtocol);
 
				byte[] data = stream.ToArray();
				
				return data;			
			}catch(Exception ex){
				log.Error("Error marshalling message", ex);
			}
			
			return null;
		}
		
		public short ProtocolType{
			get{ return 2;}
		}
		
		public short ProtocolVersion{
			get{ return 0;}
		}
		
	}
}
