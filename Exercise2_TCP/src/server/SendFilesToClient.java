package server;
import java.net.*;
import java.io.*;

public class SendFilesToClient implements Runnable{
	private Socket connection;
	private File rootDirectory;
	
	public SendFilesToClient(Socket c, File rd) {
		this.connection = c;
		this.rootDirectory = rd;
	}
	
	public void run() {
		while(true) {
			boolean closeConnectionAfterService = true; // don't close connection when client sends index successfully
			try{
				BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));// reader for clien't connection

				int num_lines = Integer.parseInt(reader.readLine()); 
				StringBuilder message = new StringBuilder();
				for (int i=0; i<num_lines; i++) {
					message.append(reader.readLine());
					if (i<num_lines-1) message.append("\r\n"); // only add upto last-1 line
				}
				String request = message.toString();
				System.out.println("Client sent request = " + request);
				if (request.equals("index")) {
					// send list of files
					BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream())); // writer to client's socket
					String [] listOfFiles = rootDirectory.list();
					num_lines = listOfFiles.length;
					String response = String.join("\r\n", listOfFiles);
					writer.write(Integer.toString(num_lines) + "\r\n");
					writer.write(response);
					writer.write("\r\n");
					writer.flush();
					closeConnectionAfterService = false;
				}else {
					//assume client wants to get a file and sends get filename
					BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream())); // writer to client's socket
					String [] requestWords = request.split(" "); // split client's request into words. It must send get filename.txt
					if (!requestWords[0].equals("get") | requestWords.length!=2) {
						num_lines = 1;
						writer.write(Integer.toString(num_lines)+ "\r\n");
						writer.write("error" + "\r\n");
						writer.flush();
					}else {
						String reqfilename = requestWords[1];
						File [] listOfFiles = rootDirectory.listFiles();
						File myFile = null;
						//check if filename is in listOfFiles
						boolean fileFound = false;
						for (File file: listOfFiles) {
							String filename = file.getName();
//							System.out.println(filename);
							if (filename.equals(reqfilename)) {
								fileFound = true;
								myFile = file; // assign myFile to the requested File
								break;
							}
						}
						if (!fileFound) {
							num_lines = 1;
							writer.write(Integer.toString(num_lines)+ "\r\n");
							writer.write("error" + "\r\n");
							writer.flush();
						}else {
							// file found
							BufferedReader filereader = new BufferedReader(new FileReader(myFile));
							StringBuilder contents = new StringBuilder();
							String line = filereader.readLine();
							if (line==null)
								num_lines = 0; // file may be empty
							else {
								num_lines = 1;
								contents.append(line); // first line
							}
							line = filereader.readLine();
							
							while(line!= null) {
								contents.append("\r\n");
								contents.append(line);
								num_lines += 1;
								line = filereader.readLine();
							}
							filereader.close();
							writer.write(Integer.toString(num_lines+1) + "\r\n"); // one extra for ok
							writer.write("ok" + "\r\n");
							writer.write(contents.toString() + "\r\n");
							writer.flush();
						}
					}
					
				}
			}catch(IOException ex) {
				//can't open writer to client.
				ex.printStackTrace();
			}catch(Exception ex2){ex2.printStackTrace(); }finally {
				if (closeConnectionAfterService) {
					if(connection!=null) try {//close connection only if reader or writer can't be opened. inside tasks will close their own connections.
						connection.close();		
						break; // break from while loop. client's work is done.
					}catch(IOException ex2) {System.out.println("Can't close connection.");}
				}
			}
		}
		}
}
