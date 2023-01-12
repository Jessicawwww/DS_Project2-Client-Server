import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.util.TreeMap;

public class RemoteVariableServerTCP {
    private int sum, id;
    private TreeMap<Integer, Integer> hashMap;
    private int port; // 7777
    private ServerSocket aSocket;
    private Socket clientSocket;

    public RemoteVariableServerTCP() throws IOException {
        sum = 0;
        port = 7777;
        hashMap = new TreeMap<>();
        aSocket = new ServerSocket(port);
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
     * subtract number for each client ID
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
            RemoteVariableServerTCP serverTCP = new RemoteVariableServerTCP();
            serverTCP.clientSocket = serverTCP.aSocket.accept();
            Scanner in = new Scanner(serverTCP.clientSocket.getInputStream());
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(serverTCP.clientSocket.getOutputStream())));
            String operationString = "";
            while(true){
                // receive from client
                if (serverTCP.clientSocket==null){
                    serverTCP.clientSocket = serverTCP.aSocket.accept();
                    in = new Scanner(serverTCP.clientSocket.getInputStream());
                    out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(serverTCP.clientSocket.getOutputStream())));
                }
                try {
                    operationString = in.nextLine();
                } catch(Exception e){
                    serverTCP.clientSocket.close();
                    serverTCP.clientSocket = null;
                    continue;
                }
                // extract elements from string
                int id = Integer.parseInt(operationString.split(" ")[0]);
                int option = Integer.parseInt(operationString.split(" ")[1]);
                int result = 0;
                int value = 0;
                switch (option){
                    case 1:
                        value = Integer.parseInt(operationString.split(" ")[2]);
                        result = serverTCP.addNum(id, value);
                        break;
                    case 2:
                        value = Integer.parseInt(operationString.split(" ")[2]);
                        result = serverTCP.subNum(id, value);
                        break;
                    case 3:
                        result = serverTCP.hashMap.get(id);
                        break;
                    default:
                        System.out.println("invalid option input! Will return 0 for this option.");
                }
                System.out.println("Client ID: "+id+"; Operation Request: "+option+"; Returned Value: "+result);
                out.println(result+"\n");
                out.flush();
            }
        }catch (IOException e) {System.out.println("IO: " + e.getMessage());
        }
    }
}
