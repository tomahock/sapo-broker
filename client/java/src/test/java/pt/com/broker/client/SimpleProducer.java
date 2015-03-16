package pt.com.broker.client;

public class SimpleProducer {

	public static void main(String[] args) throws InterruptedException {
		// TODO Auto-generated method stub
		BrokerClientProducer p = new BrokerClientProducer("127.0.0.1", 3323);
		while(true){
			p.produceMessage("Testing Node Client", "/sapo/broker/dev/test_node");
			Thread.sleep(1000);
		}
	}

}
