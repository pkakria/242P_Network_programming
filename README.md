# 242P_Network_programming
This is a Networking Programming course in UCI-MSWE(Fall 2020)

**Exercise1_lineCounts**
Write a program that will count the number of lines in each file that is specified on the command line. Assume that the files are text files. Note that multiple files can be specified, as in "java LineCounts file1.txt file2.txt file3.txt". Write each file name, along with the number of lines in that file, to standard output. If an error occurs while trying to read from one of the files, you should print an error message for that file, but you should still process all the remaining files.

**Exercise2_TCP**
For this exercise, you will write a network file server program. Your program is expected to run on top of the TCP protocol. The program is a simple file server that makes a collection of files available for transmission to clients. When the server starts up, it needs to know the name of the directory that contains the collection of files. This information can be provided as a command-line argument. You can assume that the directory contains only regular files (that is, it does not contain any sub-directories). You can also assume that all the files are text files.

When a client connects to the server, the server first reads a one-line command from the client. The command can be the string "index". In this case, the server responds by sending a list of names of all the files that are available on the server. Or the command can be of the form "get <file>", where <file> is a file name. The server checks whether the requested file actually exists. If so, it first sends the word "ok" as a message to the client. Then it sends the contents of the file and closes the connection. Otherwise, it sends the word "error" to the client and closes the connection.
  
  **Exercise_UDP**
 For this exercise, you will revise your network file server program from module 3 to work on top of the UDP protocol. That is, you will need to use the DatagramSocket class. You may have to revise the threading model as well as the reader/writer constructs that you used in your prior implementation. You program also needs to overcome the following two challenges (1) UDP datagram has a size limit; your program needs to break the large files into smaller chunks that can be transported using UDP.  (2) UDP is unreliable; your program needs to implement the additional logic on top of the UDP protocol to ensure files are transmitted reliably on top of this protocol. 
