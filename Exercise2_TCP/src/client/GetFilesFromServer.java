package client;
import java.net.*;
import java.io.*;

public class GetFilesFromServer {
	public static final String DEFAULT_HOSTNAME = "localhost";
	public static final int DEFAULT_PORT = 1044;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String hostname = args.length>0 ? args[0]: DEFAULT_HOSTNAME;
		int port = args.length>1 ? Integer.valueOf(args[1]): DEFAULT_PORT;
		Socket so = new Socket();
		SocketAddress address = new InetSocketAddress(hostname, port);
		try {
			so.connect(address, port);

			while(true) {
				// keep doing this.
				//ask user for what it wants to ask the server
				BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
				boolean userInputSuccess = false;
				int userOption = 0;
				while (!userInputSuccess) {
					System.out.println("------------------------------------------------------");
					System.out.println("Hi, what would you like to request from the server?");
					System.out.println("To get index, input 1. To get a specific file, input 2");
					try {
						 userOption = Integer.valueOf(userInput.readLine()); // hopefully can read user's 
						 if (userOption == 1 | userOption == 2) {
							 userInputSuccess = true;
						 }else {
							 System.out.println("Please enter either 1 or 2");
						 }
					}catch(NumberFormatException ex) {
						System.out.println("Invalid entry. Please try again");
					}
				}
				if (userOption == 1) {
					//send index
					BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(so.getOutputStream(), "UTF-8"));// to send to server
					String message = "index" + "\r\n";
					int num_lines = 1;
					
					writer.write(Integer.toString(num_lines) + "\r\n");
					writer.write(message);
					writer.flush();
					
					BufferedReader reader = new BufferedReader(new InputStreamReader(so.getInputStream(), "UTF-8")); // to receive from server

					int num_lines_to_read = Integer.parseInt(reader.readLine());
					StringBuilder serverReply = new StringBuilder();
					for (int i=0; i<num_lines_to_read; i++) {
						serverReply.append(reader.readLine());
						if (i<num_lines_to_read-1) serverReply.append("\r\n"); // only add upto last-1 line
					}
					String receivedMessage = serverReply.toString();
					
					System.out.println("Msg Sent: Index");
					System.out.println("Msg received: ");
					System.out.println(receivedMessage);
				}else if(userOption == 2) {
					System.out.println("Please input the filename for which to get contents: ");
					String filename = userInput.readLine();
					BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(so.getOutputStream(), "UTF-8"));// to send to server
					String message = "get " + filename + "\r\n";
				    
					int num_lines = 1;
					writer.write(Integer.toString(num_lines) + "\r\n");
					writer.write(message);
					writer.flush();

					BufferedReader reader = new BufferedReader(new InputStreamReader(so.getInputStream(), "UTF-8")); // to receive from server
					int num_lines_to_read = Integer.parseInt(reader.readLine());
					StringBuilder serverReply = new StringBuilder();
					for (int i=0; i<num_lines_to_read; i++) {
						serverReply.append(reader.readLine());
						if (i<num_lines_to_read-1) serverReply.append("\r\n"); // only add upto last-1 line
					}
					String receivedMessage = serverReply.toString();

					System.out.println("Msg sent: "+ message);
					System.out.println("Msg received: ");
					System.out.println(receivedMessage);
					reader.close();
				}
			}
		}catch(IOException ex) {
			System.err.println("Connection exception. Server closed the connection. Exiting.");
			//ex.printStackTrace();
		}finally {
			try {
				so.close();
			}catch(IOException ex) { 
				System.out.println("Exception caught. Can't close socket.");
				//ignore
				}
		}	
	}

}
