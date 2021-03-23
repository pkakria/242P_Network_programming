package linecounts;
import java.io.*;
import java.io.IOException;


public class LineCounts {

	public LineCounts() { 
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args)  {
		// TODO Auto-generated method stub
		         int i = 0;
		         System.out.println("Hello");
		         for (i=0; i<args.length; i++)
		         {
		            String filename = args[i];
		            try(BufferedReader br = new BufferedReader(new FileReader(filename))){
			            int linecount = 0;
			            String newline = br.readLine();
			            while(newline!= null){
			                linecount += 1;
			               // System.out.println(newline);
			                newline = br.readLine();
			            }
			            System.out.println(args[i] + " " + linecount + " lines");
		            }catch(FileNotFoundException ex) {
		            	System.err.println("File " + filename + " not found");
		            }catch(IOException ex) {
		            	System.err.println("Cannot write to the file");
		            	ex.printStackTrace();
		            }
		         }
		     }

	}

