package testUDP;
import java.net.*;
import java.io.*;

public class SendFileThread implements Runnable{
	DatagramPacket dp;
	File fileToSend;
	int sendToPort;
	int receivingPort;
	int newreceivingPort;
	byte [] fileDataBytes; // for other thread's access purpose
	int bytesPerBlock;// for other thread's access
	DatagramSocket ds; // for other thread's access
	int truncfileSizeInBytes;
	int packetSize;
	InetAddress sendToAddress;
	ReceiveAckThread ackreceiver;
	
	public SendFileThread(DatagramPacket dp, int receivingPort, File f) {
		// TODO Auto-generated constructor stub
		this.dp = dp;
		this.fileToSend = f;
		this.sendToPort = dp.getPort();
		this.sendToAddress = dp.getAddress();
		this.receivingPort = receivingPort;
		this.ackreceiver = new ReceiveAckThread();
	}


	public void run() {
		try {
		FileInputStream fs = new FileInputStream(fileToSend);
		
		long fileSizeInBytes = fileToSend.length();
		if(fileSizeInBytes > 1e9) {
			System.out.println("File too big to send. Can't send.");
			fs.close();
			return;
		}
		truncfileSizeInBytes = (int) fileSizeInBytes;
		byte [] fileData = new byte[truncfileSizeInBytes]; // this will be the actual file Data.
		bytesPerBlock = 509;
		int numBlocks = (int)Math.floor(truncfileSizeInBytes/bytesPerBlock) + 1; //if fully divisble, we are sending one extra empty packet
		System.out.println("numBlock in file = " + numBlocks);

		// start a sending socket on a random port between 10000 and 12000.
		int randomPort = (int)Math.floor(Math.random()*2000) + receivingPort + 1;
		ds = new DatagramSocket(randomPort);
		newreceivingPort = randomPort;
		
		//read data into fileData array
		int bytesRead = -1;
		int blockNumRead = 0;
		byte [] PaddedFileData = new byte[numBlocks*bytesPerBlock]; // read into a larger array so read doesen't give an error

		for (int i = 0; i<numBlocks; i++) {
			bytesRead = fs.read(PaddedFileData, i*bytesPerBlock, bytesPerBlock);
			blockNumRead +=1;
			if (bytesRead < bytesPerBlock)
				break;
		}
		if (bytesRead == -1) {
			System.out.println("Last block is empty");
			assert(blockNumRead == numBlocks);
		}
		try {
			fs.close();
		}catch(IOException ioex) {
			System.out.println("Cannot close the opened file. Error. Keep a log");
		}
		System.arraycopy(PaddedFileData, 0, fileData, 0, truncfileSizeInBytes); // only copy the front part of the array, leave the zeros at the end

		fileDataBytes = fileData;
		//now start ack thread. acks will come as I am sending data
		ackreceiver.setnumOfBlocks(numBlocks);
		ackreceiver.setReceivingPort(randomPort);
		ackreceiver.setDatagramSocket(ds);
		Thread ackthread = new Thread(ackreceiver);
		ackthread.start();
		
		//send data
		packetSize = 512;
		byte [] data = new byte [bytesPerBlock];
		byte [] packet = new byte [packetSize];
		
		for (int i=0; i<numBlocks; i++) {
			if (i<numBlocks-1) {
				System.arraycopy(fileData, i*bytesPerBlock, data, 0, bytesPerBlock);
				System.arraycopy(data, 0, packet, 3, bytesPerBlock); // copy data into packet leaving first three bytes free
				//System.out.println("1sending " + data.length + " bytes of data");
			}else {
				//last one special case
				if (Math.floorMod(fileData.length, bytesPerBlock) == 0) {
					data = new byte [0];
					packet = new byte[3]; // no data
					//System.out.println("2sending " + data.length + " bytes of data");
				}else {
					data = new byte [truncfileSizeInBytes - (numBlocks-1)*bytesPerBlock];
					packet = new byte[data.length + 3];
				System.arraycopy(fileData, (numBlocks-1)*bytesPerBlock, data, 0, data.length); // copy remaining part of the fileData
				System.arraycopy(data, 0, packet, 3, data.length); // copy the 
				//System.out.println("3sending " + data.length + " bytes of data");
				}
			}
			// add header to packet
			//data packet
			packet[0] = (byte)3;     //operation of dp
			//block number
			packet[2] = (byte)(i+1); // should take only the first 8 bits    lsb/msb
			packet[1] = (byte)((i+1)>>8); // should take the higher 8 bits
		
			//System.out.println("Client sees " + reconst_blocknum);
			DatagramPacket dp = new DatagramPacket(packet, packet.length, sendToAddress, sendToPort);
			synchronized(ds) {
				ds.send(dp);
				//System.out.println(dp.getLength());
				System.out.println("sent blocknumber " +(i+1));
			}
			(new Thread(new RetransmitMonitorThread(i+1, ackreceiver, this))).start();// start retransmit monitoring timer
		}
		while(ackthread.isAlive()) {
			Thread.sleep(10);
		}
		ds.close();
		}catch(FileNotFoundException fex) {
			System.out.println("File not found. Returning.");
		}catch(SocketException sex2) {
			System.out.println("Cannot open Socket on the desired port. Returning.");
		}catch(IOException ioex) {
			ioex.printStackTrace();
		}catch(InterruptedException iex) {
			iex.printStackTrace();
			System.out.println("Thread interruped");
		}
	}
	
}
