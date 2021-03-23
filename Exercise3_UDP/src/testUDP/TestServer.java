package testUDP;
import java.net.*;
import java.io.*;

public class TestServer {
	static final int port = 10000;
	static final File errorFile = new File("rootDirectory/error.txt");
	
	public TestServer() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
				File rootDirectory = new File ("rootDirectory/");
				try {
				DatagramSocket ds = new DatagramSocket(port);
				while(true) {
				byte [] data = new byte[512]; // max packet size
				DatagramPacket dp = new DatagramPacket(data, data.length);
				
				ds.receive(dp);
				assert((int)data[0] == 1); //operation code of request
				//building filename
				int iter = 1;//from first byte onwards filename exists in ASCII encoding
				while(data[iter]!=0) {// after filename, next byte is 0
					iter +=1;
				}
				
				StringBuilder filename = new StringBuilder();
				for (int i=1; i<iter; i++)
					filename.append((char)data[i]);
				
				String filenameString = filename.toString();
				System.out.println("Client request filename = " + filenameString);
				String [] filenametokens = filenameString.split(" ");
				//System.out.println("filenametokens"+filenametokens);
				
				// check if client is asking for index or index.txt
				//update index.txt if client is asking for index
				if (filename.toString().equals("index") | filename.toString().equals("index.txt")) {
					FileWriter fw = new FileWriter("rootDirectory/index.txt");
					String [] indexOfFiles = rootDirectory.list();
					for (String str: indexOfFiles) {
						fw.write(str);
						fw.write(System.lineSeparator());
					}
					fw.close();
					filenameString = "index.txt"; // both index and index.txt now look for this file.
				}else if (filenametokens[0].equals("get")) {
					filenameString = filenametokens[1]; // just the filename, remove get
				}
				//find requested file 
				File fileRequested = null;
				boolean foundFile = false;
				File [] listOfFiles = rootDirectory.listFiles();
				for (int i=0; i<listOfFiles.length; i++) {
					if (listOfFiles[i].getName().equals(filenameString)){
						fileRequested = listOfFiles[i];
						foundFile = true;
						break;
					}
				}
				if (foundFile){
					SendFileThread clientServer = new SendFileThread(dp, port, fileRequested);
					(new Thread(clientServer)).start(); // start sending process
				}else {
					SendFileThread clientServer = new SendFileThread(dp, port, errorFile);
					(new Thread(clientServer)).start(); // start sending process
				}
				
				
				}
			}catch(SocketException ex) {
				ex.printStackTrace();
				System.out.println("Can't open DatagramSocket.");
			}catch(IOException ex) {
				ex.printStackTrace();
				System.out.println("Cannot receive data due to exception.");
			}
		}
	

	}
