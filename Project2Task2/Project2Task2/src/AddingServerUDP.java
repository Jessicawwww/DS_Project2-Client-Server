import java.net.*;
import java.io.*;
import java.util.Scanner;
/**
 * Server's responsibility:
 * 1. hold an int value sum=0, receive requests from client: an int added to sum.
 * 2. upon each request, response new sum to client.
 * 3. upon each visit, display client's request and new sum.
 * 4. listen for a socket connection code separated from addition operation
 */
public class AddingServerUDP{
    public static int sum = 0;
    public static int port = 6789;
    private static int addNum(int addedNum){
        sum += addedNum;
        return sum;
    }
    public static void main(String args[]){
        System.out.println("Server started.");
        DatagramSocket aSocket = null;
        byte[] buffer = new byte[1000];
        try{
            aSocket = new DatagramSocket(port);
            DatagramPacket request = new DatagramPacket(buffer, buffer.length);
            while(true){
                // get request from client
                aSocket.receive(request);
                String addedNum = new String(request.getData()).substring(0, request.getLength());
                System.out.println("Adding "+addedNum+" to "+sum);
                // add number to sum
                sum = addNum(Integer.valueOf(addedNum));
                System.out.println("Returning sum of "+sum+" to client.");
                // reply the client with updated sum
                byte[] bytes = String.valueOf(sum).getBytes();
                DatagramPacket reply = new DatagramPacket(bytes, bytes.length, request.getAddress(), request.getPort());
                aSocket.send(reply);
            }
        }catch (SocketException e){System.out.println("Socket: " + e.getMessage());
        }catch (IOException e) {System.out.println("IO: " + e.getMessage());
        }finally {if(aSocket != null) aSocket.close();}
    }
}

