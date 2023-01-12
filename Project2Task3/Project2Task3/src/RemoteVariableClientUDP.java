import java.net.*;
import java.io.*;
import java.util.Scanner;
/**
 * Client Responsibility:
 * 1. request add, subtract, or get(idempotent).
 * 2. each request pass along an int ID to uniquely identify the user.
 * 3. client form a packet: ID, operation (add or subtract or get), and value (if the operation is other than get); server do the computation(with ID).
 * 4. menu-driven.
 * 5. repeatedly ask the user for userID, operation, value.
 *  - get: value held on the server is simply returned
 *  - add/subtract: do the operation and return sum
 * 6. display each returned value from server to user.
 * 7. for new ID, sum=0.
 * 8. ID range: 0~999.
 * 9. exit option on menu has no impact on server.
 */
public class RemoteVariableClientUDP{
    private int port = 6789;
    private InetAddress aHost;
    private DatagramSocket aSocket;
    public RemoteVariableClientUDP(int portNum) throws UnknownHostException, SocketException {
        port = portNum;
        aHost = InetAddress.getByName("localhost");
        aSocket = new DatagramSocket();
    }

    /**
     * output the menu and initialize variables
     */
    public void init(){
        System.out.println("1. Add a value to your sum.\n" +
                    "2. Subtract a value from your sum.\n" +
                    "3. Get your sum.\n" +
                    "4. Exit client");
    }

    /**
     * Generate information request based on user's choice
     * @param operationNum
     * @return data packet of client to be sent
     */
    public String start(int operationNum,  Scanner sc) {
        int option = operationNum;
        int value = 0;
        String valueString = "";
        int id;
        switch (option){
            case 1:
                System.out.println("Enter value to add: ");
                value = sc.nextInt();
                valueString = String.valueOf(value);
                break;
            case 2:
                System.out.println("Enter value to subtract:");
                value = sc.nextInt();
                valueString = String.valueOf(value);
                break;
            case 4:
                System.out.println("Client side quitting. The remote variable server is still running.");
                return "halt";
        }
        System.out.println("Enter your ID: ");
        id = sc.nextInt();
        return String.valueOf(id)+" "+String.valueOf(option)+" "+valueString;
    }

    /**
     * all socket communication code, not performing any operation
     * request the server to perform operation: add, subtract, get
     * @param dataPacket
     * @return the cumulative sum
     */
    public boolean communicate(String dataPacket){
        // get byte array of input int
        byte[] bytes = dataPacket.getBytes();
        try {
            // data packet to send to server
            DatagramPacket request = new DatagramPacket(bytes,  bytes.length, aHost, port);
            aSocket.send(request);
            // data packet to receive
            byte[] buffer = new byte[1000];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
            aSocket.receive(reply);
            String replyResult = new String(reply.getData()).substring(0, reply.getLength());

            if (replyResult.equals("halt")) {
                return false;
            }
            System.out.println("The result is "+replyResult+".");
        } catch (IOException e) {
            System.out.println("IO: "+e.getMessage());
        }
        return true;
    }


    /**
     * call on local add method to make an RPC
     * @param args
     */
    public static void main(String args[]){
        try {
            System.out.println("The client is running.");
            Scanner sc = new Scanner(System.in);
            // ask the user to input server port
            System.out.println("Please enter server port: ");
            int port = sc.nextInt();
            RemoteVariableClientUDP clientUDP = new RemoteVariableClientUDP(port);

            boolean sendFlag = true;
            while (sendFlag) {
                clientUDP.init();
                int operationNum = sc.nextInt();
                // generate message sent to server
                String operationString = clientUDP.start(operationNum, sc);
                if (operationString.equals("halt")){
                    sendFlag = false;
                    break;
                }
                // send message to server
                sendFlag = clientUDP.communicate(operationString);
            }
            if(clientUDP.aSocket != null) clientUDP.aSocket.close();

        } catch (SocketException e) {
            throw new RuntimeException(e);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
}
