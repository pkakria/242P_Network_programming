package server;
import java.io.*;
import java.net.*;

public class Server {
	public static final int DEFAULT_PORT = 1044; //445;//135;
	public static final File rootDirectory = new File("/Users/priyanka/Downloads/client-server-TCP-3/rootDirectory");
	public static void main(String[] args) {
		// TODO Auto-generated method stub
			try(ServerSocket server = new ServerSocket(DEFAULT_PORT)){
				while(true) {
				try {
					Socket connection = server.accept();
//					System.out.println("ACcepted connection");
					SendFilesToClient sender = new SendFilesToClient(connection, rootDirectory);
					Thread task = new Thread(sender);
					task.start();
//					System.out.println("Spawned task");
				}catch(IOException ex) {
					System.out.println("could not accept connection to client");
					ex.printStackTrace();
				}
			}
			}catch(IOException ex) {
				ex.printStackTrace();
			}
	}

}
