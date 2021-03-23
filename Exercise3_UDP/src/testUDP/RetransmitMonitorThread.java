package testUDP;
//for each block this thread runs
public class RetransmitMonitorThread implements Runnable{
	public final int blockNumber;
	public ReceiveAckThread ackreceiver; // reference to ackreceiver thread to monitor whose ack has arrived.
	public SendFileThread mainthread;
	
	public RetransmitMonitorThread(int blockNumber, ReceiveAckThread ackrxthread, SendFileThread mainthread) {
		// TODO Auto-generated constructor stub
		this.blockNumber = blockNumber;
		this.ackreceiver = ackrxthread;
		this.mainthread = mainthread;
	}
	
	public void run() {
		try {
			Thread.sleep(1000);// wait for 1000 msec for acknowledgement to come otherwise start retransmission thread.
			if (ackreceiver.AckReceived[blockNumber-1]== false) {
				(new Thread(new RetransmitterThread(mainthread, ackreceiver, blockNumber))).start();
			}
		}catch(InterruptedException iex) {
			iex.printStackTrace();
		}

		
	}

}
