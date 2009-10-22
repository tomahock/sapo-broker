
using System;

namespace SapoBrokerClient.Networking
{
	/// <summary>
	/// MessageAccumulator is an auxiliary class where message portions are accumulated until a full message is received.
	/// </summary>
	
	public class MessageAccumulator
	{
		public enum AccumatingStage { HEADER, BODY};
		
		public class DecodedMessageHeader
		{
			public int Length;
			public short EncodingType;
			public short EncodingVersion;
		}
		
		
		
		private AccumatingStage stage = AccumatingStage.HEADER;
		private int desiredBytes = 8; // Header size
		private int receivedBytes = 0;
		private byte[] header = new byte[8];
		private byte[] payload;
		private DecodedMessageHeader messageHeader = new MessageAccumulator.DecodedMessageHeader();
		

		public AccumatingStage Stage {
			get {
				return stage;
			}
			set {
				stage = value;
			}
		}

		public int DesiredBytes {
			get {
				return desiredBytes;
			}
			set {
				desiredBytes = value;
			}
		}

		public byte[] Header {
			get {
				return header;
			}
			set {
				header = value;
			}
		}

		public byte[] Payload {
			get {
				return payload;
			}
			set {
				payload = value;
			}
		}

		public int ReceivedBytes {
			get {
				return receivedBytes;
			}
			set {
				receivedBytes = value;
			}
		}
		
		public DecodedMessageHeader MessageHeader
		{
			get{ return messageHeader;}
		}
	}
}
