import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
//import java.util.Random;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class Client {

    //size of the reply codes from the server
    //(reply code indicates whether the client request was accepted or rejected by server)
    private final static int SERVER_CODE_LENGTH = 1;

    public static void main(String[] args) throws IOException{

        if (args.length != 2) {
            System.err.println("Usage: java Client <server_IP> <server_port>");
            System.exit(0);
        }

        int serverPort = Integer.parseUnsignedInt(args[1]);
        String serverAddr = args[0];

        String command;
        do{
            Scanner keyboard = new Scanner(System.in);
            System.out.println("enter a command (D, G, L, R, or Q):");
            //Commands are NOT case-sensitive.
            command = keyboard.next().toUpperCase();
            keyboard.nextLine();

            String fileName;

            switch (command){
                case "L":
                    //List all files (ignoring directories) in the server directory
                    //(file name : file size)
                    ByteBuffer buffer = ByteBuffer.wrap("L".getBytes());
                    SocketChannel channel = SocketChannel.open();
                    channel.connect(new InetSocketAddress(serverAddr, serverPort));
                    //System.out.println("TCP connection established.");

                    //The random sleep is for testing purpose only!
                    // try {
                    //    Thread.sleep(new Random().nextInt(20000));
                    // }catch(InterruptedException e){;}

                    //read from the buffer into the channel
                    channel.write(buffer);

                    //before writing to buffer, clear buffer
                    //("position" set to zero, "limit" set to "capacity")
                    buffer.clear();

                    int bytesRead;
                    //read will return -1 if the server has closed the TCP connection
                    // (when server has done sending)
                    if (serverCode(channel).equals("F")){
                        System.out.println("Server rejected the request.");
                    }else {
                        ByteBuffer data = ByteBuffer.allocate(1024);
                        while( (bytesRead = channel.read(data)) != -1) {
                            //before reading from buffer, flip buffer
                            //("limit" set to current position, "position" set to zero)
                            data.flip();
                            byte[] a = new byte[bytesRead];
                            //copy bytes from buffer to array
                            //(all bytes between "position" and "limit" are copied)
                            data.get(a);
                            String serverMessage = new String(a);
                            System.out.println(serverMessage);
                        }
                    }
                    channel.close();
                    break;









                case "D": //Delete a file
                    //Ask the user for the file name
                    System.out.println("enter file name you want to delete:");
                    fileName = keyboard.nextLine();

                    //Create tcp channel + connect to server
                    channel = SocketChannel.open();
                    channel.connect(new InetSocketAddress(serverAddr, serverPort));

                    //make buffer
                    buffer = ByteBuffer.wrap( ("D"+fileName).getBytes() );

                    //send buffer to server
                    channel.write(buffer);

                    //shut down sending to server
                    channel.shutdownOutput();

                    //delete said file in server

                    //Receive server response
                    //Notify the user whether the operation is successful
                    if (serverCode(channel).equals("F"))
                    {
                        System.out.println("Server rejected the request.");
                    }
                    else
                    {
                        System.out.println("Server accepted the request.");
                    }

                    channel.close();
                    break;








                case "G": //Get a file from the server
                    //Ask the user for the file name
                    System.out.println("enter file name you want to get:");
                    fileName = keyboard.nextLine();

                    //Create tcp channel + connect to server
                    channel = SocketChannel.open();
                    channel.connect(new InetSocketAddress(serverAddr, serverPort));

                    //make buffer
                    buffer = ByteBuffer.wrap( ("G"+fileName).getBytes() );

                    //send buffer to server
                    channel.write(buffer);

                    //shut down sending to server
                    channel.shutdownOutput();

                    //download
                    //create empty file to write to
                    //need to move this into downloaded folder
                    Path downloadDirectoryPath = Paths.get("downloaded files");
                    File acceptedFile = new File(String.valueOf(downloadDirectoryPath), fileName);
                    FileWriter fileWriter = new FileWriter(acceptedFile);


                    if (serverCode(channel).equals("F"))
                    {
                        System.out.println("Server rejected the request.");
                    }
                    else
                    {


                        ByteBuffer data = ByteBuffer.allocate(1024);
                        while( (bytesRead = channel.read(data)) != -1) {
                            //before reading from buffer, flip buffer
                            //("limit" set to current position, "position" set to zero)
                            data.flip();
                            byte[] a = new byte[bytesRead];
                            //copy bytes from buffer to array
                            //(all bytes between "position" and "limit" are copied)
                            data.get(a);
                            String line = new String(a);

                            //write to lines to empty file everytime its read
                            fileWriter.write(line);
                        }
                        fileWriter.close();
                        System.out.println("Server accepted the request.");
                    }




                    //Notify the user whether the operation is successful
                    break;








                case "R": //Rename a file
                    //Ask the user for the original file name
                    System.out.println("enter file name you want to rename:");
                    fileName = keyboard.nextLine();
                    //and the new file name.
                    System.out.println("enter new name for file:");
                    String newfileName = keyboard.nextLine();

                    //Create tcp channel + connect to server
                    channel = SocketChannel.open();
                    channel.connect(new InetSocketAddress(serverAddr, serverPort));

                    //make buffer
                    buffer = ByteBuffer.wrap( ("R"+fileName+"###"+newfileName).getBytes() );

                    //send buffer to server
                    channel.write(buffer);

                    //shut down sending to server
                    channel.shutdownOutput();

                    //rename said file in server

                    //Receive server response
                    //Notify the user whether the operation is successful
                    if (serverCode(channel).equals("F"))
                    {
                        System.out.println("Server rejected the request.");
                    }
                    else
                    {
                        System.out.println("Server accepted the request.");
                    }

                    channel.close();
                    break;





                default:
                    if (!command.equals("Q")){
                        System.out.println("Unknown command!");
                    }
            }
        }while(!command.equals("Q"));
    }

    private static String serverCode(SocketChannel channel) throws IOException{
        ByteBuffer buffer = ByteBuffer.allocate(SERVER_CODE_LENGTH);
        int bytesToRead = SERVER_CODE_LENGTH;

        //make sure we read the entire server reply
        while((bytesToRead -= channel.read(buffer)) > 0);

        //before reading from buffer, flip buffer
        buffer.flip();
        byte[] a = new byte[SERVER_CODE_LENGTH];
        //copy bytes from buffer to array
        buffer.get(a);
        String serverReplyCode = new String(a);

        //System.out.println(serverReplyCode);

        return serverReplyCode;
    }
}
