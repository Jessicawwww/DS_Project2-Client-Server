import java.net.*;
import java.io.*;
import java.util.Scanner;

public class EchoServerUDP{
    public static void main(String args[]){
        DatagramSocket aSocket = null;
        byte[] buffer = new byte[1000];
        try{
            System.out.println("The server is running.");
            // create a data socket connection to transfer data
            System.out.println("Enter a port:");
            Scanner sc = new Scanner(System.in);
            int serverPort = sc.nextInt(); // 6789
            aSocket = new DatagramSocket(serverPort);
            DatagramPacket request = new DatagramPacket(buffer, buffer.length);
            while(true){
                // for server, first receive what client sent to you.
                aSocket.receive(request);
                // create a datagram packet based on what client sent.
                DatagramPacket reply = new DatagramPacket(request.getData(),
                        request.getLength(), request.getAddress(), request.getPort());
                // output what client sent
                String requestString = new String(request.getData()).substring(0, request.getLength());
                // server: display request arriving from the client.
                System.out.println("Echoing: "+requestString);
                // send back reply packet to client
                aSocket.send(reply);
                if (requestString.equals("halt!")){
                    System.out.println("Server side quitting");
                    break;
                }
            }
            // exception handling
        }catch (SocketException e){System.out.println("Socket: " + e.getMessage());
        }catch (IOException e) {System.out.println("IO: " + e.getMessage());
        }finally {if(aSocket != null) aSocket.close();}
    }
}

