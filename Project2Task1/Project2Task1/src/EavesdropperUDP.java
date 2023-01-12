/**
 * @author: Olivia Wu (jingyiw2)
 */

import java.net.*;
import java.io.*;
import java.util.Scanner;

public class EavesdropperUDP {
    /**
     * Responsibility: passive malicious player
     * 1. state it is running and ask for two ports: one to listen on and the other the server to masquerade as
     * 2. display all msgs going through it.
     * 3. eavesdrop on the wire, masquerading as the server on port 6789
     */
    public static void main(String[] args) {
        DatagramSocket aSocket = null;
        DatagramSocket eavSocket = null;
        byte[] buffer = new byte[1000];

        try{
            System.out.println("EavesdropperUDP is running.");
            InetAddress aHost = InetAddress.getByName("localhost");

            Scanner sc = new Scanner(System.in);
            System.out.println("Enter a port for EavesdropperUDP to listen on:"); // 6798
            int eavPort = sc.nextInt();
            System.out.println("Enter a port of server to masquerade as:"); // 6789
            int listenPort = sc.nextInt();

            aSocket = new DatagramSocket();
            eavSocket = new DatagramSocket(eavPort); // socket for fake port
            /*
            receive and deliver to the actual port.
             */
            DatagramPacket request = new DatagramPacket(buffer, buffer.length);
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
            while (true){
                // receive msg from client and display
                eavSocket.receive(request);
                String requestString = new String(request.getData()).substring(0, request.getLength());
                // when seeing halt request, write a line of asterisks to its console to make an alert
                // eavs does not halt but display to its console and pass the halt on to the server
                if (requestString.equals("halt!")){
                    System.out.println("***************************************************************");
                }
                System.out.println("Request from client:"+requestString);


                // send the message to actual server
                DatagramPacket message = new DatagramPacket(request.getData(), request.getLength(), aHost, listenPort);
                aSocket.send(message);

                // receive message from the actual server
                aSocket.receive(reply);
                String receiveString = new String(reply.getData()).substring(0, reply.getLength());
                System.out.println("Receive from server:"+receiveString);

                // send back to the client
                DatagramPacket message2 = new DatagramPacket(request.getData(), request.getLength(), aHost, request.getPort());
                eavSocket.send(message2);
            }
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (UnknownHostException e) {
            System.out.println("Host: "+e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        } finally {
            if (aSocket != null) aSocket.close();
            if (eavSocket != null) eavSocket.close();
        }
    }
}
