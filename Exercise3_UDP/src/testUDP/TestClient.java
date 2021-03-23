package testUDP;
import java.io.*;
import java.nio.charset.StandardCharsets;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

public class TestClient {
	static int DEFAULT_PORT = 9999;
	static int port;
	static int Server_port = 10000;
	static String ServerAddress = "localhost";
	static String hostname;
	static String DEFAULT_HOST = "localhost";
	public TestClient() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		hostname = args.length>0 ? args[0]: DEFAULT_HOST;
	    port = args.length>1 ? Integer.valueOf(args[1]): DEFAULT_PORT;
		SocketAddress address = new InetSocketAddress(hostname, port);
		try (DatagramSocket ds = new DatagramSocket(address)){
			InetAddress ia = InetAddress.getLocalHost();
			InetAddress server = InetAddress.getByName(ServerAddress);
			while(true) {
				// keep doing this.
				//ask user for what it wants to ask the server
				BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
				boolean userInputSuccess = false;
				int userOption = 0;
				while (!userInputSuccess) {
					System.out.println("------------------------------------------------------");
					System.out.println("Hi, what would you like to request from the server?");
					System.out.println("To get index, input 1. To get a specific file, input 2. To exit, input 3.");
					System.out.println("Input:");
					try {
						 userOption = Integer.valueOf(userInput.readLine()); // hopefully can read user's 
						 if (userOption == 1 | userOption == 2 | userOption == 3) {
							 userInputSuccess = true;
						 }else {
							 System.out.println("Please enter either 1, 2, or 3");
						 }
					}catch(NumberFormatException ex) {
						System.out.println("Invalid entry. Please try again");
					}
				}
			
				if (userOption == 3) {
					break;
				}else if (userOption == 1) {
					String message = "index";
					
					byte[] data = message.getBytes();
					byte[] packet = new byte[data.length+2];
					packet[0] = (byte)1; 
					System.arraycopy(data,0,packet,1,data.length);
					packet[data.length+1]= 0;
					
					DatagramPacket dgp = new DatagramPacket(packet,packet.length,server,Server_port);
					ds.send(dgp);
					FileWriter fw = new FileWriter("index.txt");
					int result = receiveData(fw,ds);
					fw.close();
					if (result==-1) {
						System.out.println("Client could not receive index due to exception.");
					}else {
						System.out.println("**************************************************************");
						System.out.println("Message received:");
						BufferedReader br = new BufferedReader(new FileReader("index.txt"));
						String line = br.readLine();
						while(line!=null) {
							System.out.println(line);
							line = br.readLine();
						}
						br.close();
					}
				}else if(userOption == 2) {
					System.out.println("Please input the filename for which to get contents: ");
					String filename = userInput.readLine();
				
					String message = "get " + filename;
					byte[] data = message.getBytes();
					byte[] packet = new byte[data.length+2];
					packet[0] = (byte)1;   //casting operation code
					System.arraycopy(data,0,packet,1,data.length);
					packet[data.length+1]= 0;
					DatagramPacket dgp = new DatagramPacket(packet,packet.length,server,Server_port);
					ds.send(dgp);
					FileWriter fw = new FileWriter(filename);
					int result = receiveData(fw,ds);
					fw.close();
					
					if (result==-1) {
						//error occured in packet recpetion
						System.out.println("Client could not receive file due to exception.");
					}else {
						System.out.println("**************************************************************");
						System.out.println("Message Received:");
						BufferedReader br = new BufferedReader(new FileReader(filename));
						String line = br.readLine();
						while(line!=null) {
							System.out.println(line);
							line = br.readLine();
						}
						br.close();

					}
					
				}
			}
		}catch(IOException ex) {
			System.err.println("Connection exception");
			ex.printStackTrace();
		}
	}

	public static int receiveData(FileWriter fw,DatagramSocket ds)
	{
		//initially work with a size limit of 40000 blocks of 509 bytes each.
		int maxNumBlocks = 40000;
		int maxDataBlockSize = 509; // bytes excluding headers
		int [] packetReceived = new int[maxNumBlocks];
		for (int i=0; i<maxNumBlocks; i++) {
			packetReceived[i] = -1; // all -1 initially i.e. no block valid
		}
		int LargestNumBlockReceived = 0; // this should go from 1 up to maxNumBlocks once block start being received
		int LastBlockDataSize = 0; // last block's data size.
		try {
			DatagramSocket rcvds = ds; // keep same datagramSocket as main function.
			DatagramSocket sendingSocket = ds; // socket to send ack. same as rcvds.
			rcvds.setSoTimeout(10000);
			boolean keepreceiving = true;
			boolean lastPacketReceived = false;
			byte [] fileData = new byte [maxNumBlocks*maxDataBlockSize]; // the total file Data
			
			while(keepreceiving) {
				byte [] dgppacket = new byte [512];
				DatagramPacket dgp = new DatagramPacket(dgppacket, dgppacket.length);
				rcvds.receive(dgp);
				int lenOfPacketData = dgp.getLength(); // may be less than packet size
				byte [] packet = new byte [lenOfPacketData]; //may be shorter than packet. has headers
			
				System.arraycopy(dgppacket, 0, packet, 0, lenOfPacketData);

			
				int blockNumber = (int)((packet[1]&0xff)<<8) + (int)(packet[2]&0xff);
				
				if (lenOfPacketData < maxDataBlockSize) {
					lastPacketReceived = true;
					LastBlockDataSize = lenOfPacketData - 3;
					System.out.println("last packet");
				}
				if (blockNumber > maxNumBlocks) {
					//for now throw error
					System.out.println("Opcode = "+ (int)packet[0]);
					System.out.println("File too large. Can't receive. Blockum = " + blockNumber);
					return -1;
				}else {
					//if(data.length!=0) {
						//System.arraycopy(data, 0, fileData, (blockNumber-1)*maxDataBlockSize, data.length);
					if(lenOfPacketData!=3) {
						System.arraycopy(packet, 3, fileData, (blockNumber-1)*maxDataBlockSize, lenOfPacketData-3);
					}
					sendingSocket.setSoTimeout(10000);
					byte [] ackpacket = new byte [3];
					ackpacket[0] = (byte)4;
					ackpacket[1] = (byte)(blockNumber >>8);   //bit shift
					ackpacket[2] = (byte)(blockNumber);
					System.out.println("Received blocknumber " + blockNumber);
			
					DatagramPacket ackdgp = new DatagramPacket(ackpacket, ackpacket.length, dgp.getAddress(), dgp.getPort());
					sendingSocket.send(ackdgp);
					//mark received
					packetReceived[blockNumber-1] = 1;
					if (blockNumber > LargestNumBlockReceived)
						LargestNumBlockReceived = blockNumber;
					// if last packet has been received, check if all packets before it have been received, if not
					 boolean anyPacketLeft = false;
					if (lastPacketReceived) {
						for (int i = 0; i<LargestNumBlockReceived; i++) {
							 if (packetReceived[i]!=1) {
								 anyPacketLeft = true;
								 break;
							 }
						}
						keepreceiving = anyPacketLeft; // keep receiving while a packet is still left to be transmitted
					}
				}
				
			}
			//rcvds.close();
			int totalDataSize = (LargestNumBlockReceived-1)*maxDataBlockSize + LastBlockDataSize;
			byte [] dataToWrite = new byte [totalDataSize];
			System.arraycopy(fileData, 0, dataToWrite, 0, totalDataSize);
			String fileString = new String(dataToWrite, StandardCharsets.UTF_8);
			//System.out.println("Received message: "+ fileString);
			fw.write(fileString);
			
		}catch(SocketException ex) {
			ex.printStackTrace();
			return -1;
		}catch(SocketTimeoutException stex) {
			System.out.println("Socket timed out while waiting for server. Cannot receive full file");
			return -1;
		}catch(IOException iex) {
			iex.printStackTrace();
			return -1;
		}
			

		return 1;
	}
}
