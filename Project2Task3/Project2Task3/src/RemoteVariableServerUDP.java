import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.util.TreeMap;

/**
 * Server's responsibility:
 * 1. map each ID to value of sum using TreeMap.
 * 2. assume that ID are positive integers.
 *
 */
public class RemoteVariableServerUDP{
    private int sum, id;
    private int port;
    private DatagramSocket aSocket;
    private TreeMap<Integer, Integer> hashMap;
    private DatagramPacket request;
    public RemoteVariableServerUDP() throws SocketException{
        sum = 0;
        port = 6789;
        aSocket = new DatagramSocket(port);
        byte[] buffer = new byte[1000];
        request = new DatagramPacket(buffer, buffer.length);
        hashMap = new TreeMap<>();

    }

    /**
     * add number for each client ID
     * @param id
     * @param value
     * @return the result being returned and printed out
     */
    public int addNum(int id, int value){
        if (hashMap.containsKey(id)){
            sum = hashMap.get(id)+value;
            hashMap.put(id, sum);
        } else {
            sum = value;
            hashMap.put(id, sum);
        }
        return sum;
    }

    /**
     * subtract number for each clent ID
     * @param id
     * @param value
     * @return the result being returned and printed out
     */
    public int subNum(int id, int value){
        if (hashMap.containsKey(id)){
            sum = hashMap.get(id)-value;
            hashMap.put(id, sum);
        } else {
            sum = 0-value;
            hashMap.put(id, sum);
        }
        return sum;
    }
    public static void main(String args[]){
        System.out.println("Server started.");
        try{
            RemoteVariableServerUDP serverUDP = new RemoteVariableServerUDP();
            while(true){
                // get request from client
                serverUDP.aSocket.receive(serverUDP.request);
                String operationString = new String(serverUDP.request.getData()).substring(0, serverUDP.request.getLength());
                // extract elements from string
                int id = Integer.parseInt(operationString.split(" ")[0]);
                int option = Integer.parseInt(operationString.split(" ")[1]);
                int result = 0;
                int value = 0;
                switch (option){
                    case 1:
                        value = Integer.parseInt(operationString.split(" ")[2]);
                        result = serverUDP.addNum(id, value);
                        break;
                    case 2:
                        value = Integer.parseInt(operationString.split(" ")[2]);
                        result = serverUDP.subNum(id, value);
                        break;
                    case 3:
                        result = serverUDP.hashMap.get(id);
                        break;
                }
                System.out.println("Client ID: "+id+"; Operation Request: "+option+"; Returned Value: "+result);

                // reply to the client with updated result
                byte[] bytes = String.valueOf(result).getBytes();
                DatagramPacket reply = new DatagramPacket(bytes, bytes.length, serverUDP.request.getAddress(), serverUDP.request.getPort());
                serverUDP.aSocket.send(reply);
            }
        }catch (SocketException e){System.out.println("Socket: " + e.getMessage());
        }catch (IOException e) {System.out.println("IO: " + e.getMessage());
        }
    }
}

