package testUDP;
import java.io.IOException;
import java.net.*;

public class RetransmitterThread implements Runnable {
	int blockNumber;
	public SendFileThread mainthread;
	public ReceiveAckThread ackreceiver;
	
	public RetransmitterThread(SendFileThread mainthread,  ReceiveAckThread ackreceiver, int blockNumber) {
		// TODO Auto-generated constructor stub
		this.mainthread = mainthread;
		this.blockNumber = blockNumber;
		this.ackreceiver = ackreceiver;
	}
	
	public void run() {
		byte [] fileDataBytes = mainthread.fileDataBytes;
		int truncfileSizeInBytes = mainthread.truncfileSizeInBytes;
		int bytesPerBlock = mainthread.bytesPerBlock; // 509
		int lengthOfBlock = Math.min(bytesPerBlock, truncfileSizeInBytes - (blockNumber-1)*bytesPerBlock); // either 509 or less if this is the last block. may be 0 even
		byte [] packet = null;
		if (lengthOfBlock == 0) {
			packet = new byte [3];
		}else {
			packet = new byte[lengthOfBlock+3];
			System.arraycopy(fileDataBytes, (blockNumber-1)*bytesPerBlock, packet, 0, lengthOfBlock);
		}
		packet[0] = (byte)3; // operation code of data
		//block number
		packet[2] = (byte)(blockNumber); // should take only the first 8 bits
		packet[1] = (byte)((blockNumber)>>8); // should take the higher 8 bits
		try {
			DatagramSocket ds = mainthread.ds;
			DatagramPacket dp = new DatagramPacket(packet, packet.length, mainthread.sendToAddress, mainthread.sendToPort);
			synchronized(ds) {
				System.out.println("retransmitting block " + blockNumber);
				ds.send(dp);
			}
			(new Thread(new RetransmitMonitorThread(blockNumber, ackreceiver, mainthread))).start();// start retransmit monitoring timer
//			ds.close();
		}catch(SocketException sockex) {
			sockex.printStackTrace();
		}catch(IOException ioex) {
			ioex.printStackTrace();
		}
	}

}
