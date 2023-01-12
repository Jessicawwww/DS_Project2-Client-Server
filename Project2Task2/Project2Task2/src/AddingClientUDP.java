import java.net.*;
import java.io.*;
import java.util.Scanner;
/**
 * Client Responsibility:
 * 1. all communication code placed in method "add".
 * 2. no communications code in main.
 * 3. client add: not performing any addition, ask the server do the addition.
 * 4. add is a proxy for the server
 * 5. when calling local add method, RPC
 */
public class AddingClientUDP{
    public static int port = 6789;
    public static InetAddress aHost;
    public static DatagramSocket aSocket;
    public AddingClientUDP(int portNum){
        try {
            aSocket = new DatagramSocket();
            port = portNum;
            aHost = InetAddress.getByName("localhost");
        } catch (SocketException | UnknownHostException e){
            System.out.println("Error:"+e.getMessage());
        }
    }
    /**
     * all socket communication code, not performing any addition
     * request the server to perform addition
     * @param i
     * @return the cumulative sum
     */
    public static int add(int i){
        // get byte array of input int
        byte[] bytes = String.valueOf(i).getBytes();
        try {
            // data packet to send to server
            DatagramPacket request = new DatagramPacket(bytes,  bytes.length, aHost, port);
            aSocket.send(request);
            // data packet to receive
            byte[] buffer = new byte[1000];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
            aSocket.receive(reply);
            String replyResult = new String(reply.getData()).substring(0, reply.getLength());
            return Integer.valueOf(replyResult);
        } catch (IOException e) {
            System.out.println("IO: "+e.getMessage());
        }
        return -1;
    }
    /**
     * call on local add method to make an RPC
     * @param args
     */
    public static void main(String args[]){
        try {
            System.out.println("The client is running.");
            // ask the user to input server port
            System.out.println("Please enter server port: ");
            Scanner sc = new Scanner(System.in);
            port = sc.nextInt();
            AddingClientUDP clientUDP = new AddingClientUDP(port);
            while (sc.hasNext()) {
                if (sc.nextLine().equals("halt!")){
                    System.out.println("Client side quitting.");
                    break;
                }
                if (sc.hasNextInt()){
                    int addedNum = sc.nextInt();
                    // call add
                    int result = add(addedNum);
                    System.out.println("The server returned "+result+".");
                }
            }
        } finally {if(aSocket != null) aSocket.close();}
    }
}
