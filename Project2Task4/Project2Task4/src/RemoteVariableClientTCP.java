import java.net.*;
import java.io.*;
import java.util.Scanner;

public class RemoteVariableClientTCP {
    private int port = 7777;
    private InetAddress aHost;
    private Socket clientSocket;
    public RemoteVariableClientTCP(int portNum) throws IOException {
        port = portNum;
        aHost = InetAddress.getByName("localhost");
        clientSocket = new Socket(aHost, port);
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
        return id +" "+ option +" "+valueString;
    }

    /**
     * all socket communication code, not performing any operation
     * request the server to perform operation: add, subtract, get
     * @param dataPacket
     */
    public void communicate(String dataPacket ) {
        try {
            // send to the server through TCP
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())));
            out.println(dataPacket);
            out.flush();
            // receive from server
            String replyResult= in.readLine(); // read a line of data from the stream
            System.out.println("The result is " + replyResult + ".");
        } catch (IOException e) {
            System.out.println("IO Exception:" + e.getMessage());
        }
    }

    /**
     * proxy design
     * @param args
     */
    public static void main(String args[]){
        try {
            System.out.println("The client is running.");
            Scanner sc = new Scanner(System.in);
            // ask the user to input server port
            System.out.println("Please enter server port: ");
            int port = sc.nextInt(); // 7777
            RemoteVariableClientTCP clientTCP = new RemoteVariableClientTCP(port);

            boolean sendFlag = true;
            while (sendFlag) {
                clientTCP.init();
                int operationNum = sc.nextInt();
                // generate message sent to server
                String operationString = clientTCP.start(operationNum, sc);
                if (operationString.equals("halt")){
                    sendFlag = false;
                    break;
                }
                // send message to server
                clientTCP.communicate(operationString);
            }
        }  catch (IOException e) {
            System.out.println("IO:"+e.getMessage());
        }
    }

}