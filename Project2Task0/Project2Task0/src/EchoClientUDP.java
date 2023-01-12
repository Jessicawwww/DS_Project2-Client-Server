import java.net.*;
import java.io.*;
import java.util.Scanner;

public class EchoClientUDP{
    public static void main(String args[]){
        // args give message contents and server hostname.
        // create the socket between two parties on the network.
        DatagramSocket aSocket = null;
        try {
            System.out.println("The client is running.");
            // determine the IP address of a host from the given host's name.
            // InetAddress aHost = InetAddress.getByName(args[0]);
            InetAddress aHost = InetAddress.getByName("localhost");
            // a socket is bound to a port number so that the UDP layer can identify the application where data is sent.
            //int serverPort = 6789;
            System.out.println("Enter a port:");
            Scanner sc = new Scanner(System.in);
            int serverPort = sc.nextInt();
            // create a datagram socket and bind to any available port on local machine.
            aSocket = new DatagramSocket();
            // use buffer reader to get user's input.
            String nextLine;
            BufferedReader typed = new BufferedReader(new InputStreamReader(System.in));
            while ((nextLine = typed.readLine()) != null) {
                // extract byte array from the input string
                byte [] m = nextLine.getBytes();
                // facilitate connectionless transfer of messages from one system to another.
                DatagramPacket request = new DatagramPacket(m,  m.length, aHost, serverPort);
                // send a datagram packet(contains data to be sent) from the socket
                aSocket.send(request);
                // receive datagram packet from socket (contains IP addr and port num of sender's machine
                byte[] buffer = new byte[1000];
                DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
                aSocket.receive(reply);
                String replyString = new String(reply.getData()).substring(0, reply.getLength()); // reply.getLength()
                if (replyString.equals("halt!")) {
                    System.out.println("Client side quitting");
                    break;
                }
                System.out.println("Reply: " + replyString);
            }
        // exception handling
        }catch (SocketException e) {System.out.println("Socket: " + e.getMessage());
        }catch (IOException e){System.out.println("IO: " + e.getMessage());
        }finally {if(aSocket != null) aSocket.close();}
    }
}
