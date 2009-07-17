
using System;
using System.Collections.Generic;

using SapoBrokerClient.Encoding;
using SapoBrokerClient.Encoding.Thrift;

namespace SapoBrokerClient
{
	public static class MessageSerializerFactory
	{
		// TODO: delegate message serialization objects addition to other part of the solution.
		static MessageSerializerFactory()
		{
			addSerializer(new ThriftMessageSerializer() );
		}
		
		private static IDictionary<short, IMessageSerializer> messageSerializers = new Dictionary<short, IMessageSerializer>();
		
		public static IMessageSerializer getSerializer(short protocolType, short protocolVersion)
		{
			lock(messageSerializers){
				return messageSerializers[protocolType];
			}
		}
		
		public static void addSerializer(IMessageSerializer messageSerializer)
		{
			lock(messageSerializers){
				messageSerializers.Add(messageSerializer.ProtocolType, messageSerializer);
			}
			
		}
		
	}
}
