package testUDP;
import java.net.*;
import java.io.*;

public class ReceiveAckThread implements Runnable{
	int receivingPort;
	int numOfBlocks;
	public boolean [] AckReceived;
	DatagramSocket ds;

	
	public ReceiveAckThread() {
		this.receivingPort = 9999;
		this.numOfBlocks = 0;
	}
	public ReceiveAckThread(int port, int numOfBlocks, DatagramSocket ds) {
		// TODO Auto-generated constructor stub
		this.receivingPort = port;
		this.numOfBlocks = numOfBlocks;// server tells this thread how many block acks to expect.
		AckReceived = new boolean [numOfBlocks]; // false if not , true if received
		this.ds = ds;
	}
	
	public void setReceivingPort(int port) {
		this.receivingPort = port;
	}
	
	public void setnumOfBlocks(int numOfBlocks) {
		this.numOfBlocks = numOfBlocks;
		AckReceived = new boolean [numOfBlocks]; // false if not , true if received
		for(int i=0; i<numOfBlocks; i++)
			AckReceived[i] = false;
	}
	
	public void setDatagramSocket(DatagramSocket ds) {
		this.ds = ds;
	}
	
	public void run() {
		boolean notAllAckReceived = true;
		
			byte [] ackpacket = new byte [512];
			DatagramPacket dp = new DatagramPacket(ackpacket, ackpacket.length);
			while(notAllAckReceived) {// keep waiting to receive ack
				//System.out.println("Inside while loop of ack thread");
			
			try {
					//System.out.println("waiting for ack ");
					ds.receive(dp);
					//System.out.println("ack received ");
			}catch(IOException ioex) {
				ioex.printStackTrace();
				System.out.println("Couldn't receive a packet. Continuing to next one.");
				continue;
			}
			int opcode = ackpacket[0]; 
			
			if (opcode!=4) {
				System.out.println("Server got opcode = "+ opcode);
				System.out.println("Expected ACK, received some other packet. Will ignore this.");
				continue;
			}else {//opcode == 4 //ack
				//byte [] ackdata = new byte[3]; // just ack data
				//System.arraycopy(ackpacket, 0, ackdata, 0, 3);
				int blocknum = (int)(ackpacket[2]&0xff) + (int)((ackpacket[1]&0xff)<<8);
				synchronized(AckReceived) { // we want one thread to edit AckReceived and others to wait for reading
					AckReceived[blocknum-1] = true;
					System.out.println("Ack received for "+ blocknum);
				}
				
			}
			//search in all array, if any ack is still left, notAllAckReceived will become true
			boolean believeAcksReceived = true;
			for (int i = 0; i<numOfBlocks; i++) {
				believeAcksReceived = believeAcksReceived & AckReceived[i];
			}
			
			notAllAckReceived = !believeAcksReceived; // if believeAcksReceived is still true then all acks are received. So make notAllAcksReceived = false.
			//System.out.println(notAllAckReceived);
			}//end while
//			ds.close();
//		}
	}

}
