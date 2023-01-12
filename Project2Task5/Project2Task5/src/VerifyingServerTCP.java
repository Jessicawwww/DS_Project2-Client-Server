import java.net.*;
import java.io.*;
import java.util.HashMap;
import java.util.Scanner;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import java.util.TreeMap;

/**
 * Server side:
 * 1. two checks before servicing requests from client:
 *  - public key hash to ID
 *  - request properly signed
 *  - otherwise, return the message "Error in request"
 * 2. use SHA-256 for our hash function h()
 */

public class VerifyingServerTCP {
    private Socket clientSocket;
    private ServerSocket aSocket;
    private int sum, option;
    private String id;
    private TreeMap<String, Integer> hashMap;
    private String signature;
    // for public key
    private BigInteger e,n;
    public VerifyingServerTCP() throws IOException {
        aSocket = new ServerSocket(7777);
        hashMap = new TreeMap<>();
        sum = 0;
    }
    /**
     * check if id equals to public key hash
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public boolean check() throws IOException, NoSuchAlgorithmException {
        String en = String.valueOf(e)+String.valueOf(n);
        if (id.equals(new SigningClientTCP(7777).setId(en))){
            System.out.println("Client public key: "+"("+e+","+n+")");
            return true;
        }
        System.out.println("Error in request.");
        return false;
    }

    /**
     * Verifying proceeds as follows:
     *      1) Decrypt the encryptedHash to compute a decryptedHash
     *      2) Hash the messageToCheck using SHA-256 (be sure to handle the extra byte as described in the signing method.)
     *      3) If this new hash is equal to the decryptedHash, return true else false.
     * @param requests
     * @return whether the request is correctly encrypted.
     */
    public boolean decrypt(String [] requests) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        // Take the encrypted string and make it a big integer
        BigInteger encryptedHash = new BigInteger(signature);
        // Decrypt it
        BigInteger decryptedHash = encryptedHash.modPow(e, n);
        // concat another operation string
        String messageToCheck = "";
        for (int i=2; i<requests.length-1; i++){
            messageToCheck += (requests[i]+",");
        }
        // Get the bytes from operation string
        byte[] bytesOfMessageToCheck = messageToCheck.substring(0,messageToCheck.length()-1).getBytes("UTF-8");
        // compute the digest of the message with SHA-256
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] messageToCheckDigest = md.digest(bytesOfMessageToCheck);
        // messageToCheckDigest is a full SHA-256 digest
        // take two bytes from SHA-256 and add a zero byte
        byte[] extraByte = new byte[messageToCheckDigest.length+1];
        extraByte[0] = 0;
        for (int i =0; i<messageToCheckDigest.length; i++){
            extraByte[i+1] = messageToCheckDigest[i];
        }
        // Make it a big int
        BigInteger bigIntegerToCheck = new BigInteger(extraByte);
        // inform the client on how the two compare
        if(bigIntegerToCheck.compareTo(decryptedHash) == 0) {
            System.out.println("Valid signature.");
            return true;
        } else {
            System.out.println("Error in request.");
            return false;
        }
    }

    /**
     * get requests from client and extract elements from requestString
     * @param requests
     */
    public void init(String[] requests){
        e = new BigInteger(requests[0]);
        n = new BigInteger(requests[1]);
        id = requests[2];
        option = Integer.valueOf(requests[3]);
        signature = requests[requests.length-1];
    }
    /**
     * add number for each client ID
     * @param id
     * @param value
     * @return the result being returned and printed out
     */
    public int addNum(String id, int value){
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
    public int subNum(String id, int value){
        if (hashMap.containsKey(id)){
            sum = hashMap.get(id)-value;
            hashMap.put(id, sum);
        } else {
            sum = 0-value;
            hashMap.put(id, sum);
        }
        return sum;
    }
    public static void main(String[] args) {
        System.out.println("Server Started.");
        try {
            VerifyingServerTCP serverTCP = new VerifyingServerTCP();
            serverTCP.clientSocket = serverTCP.aSocket.accept();
            Scanner in = new Scanner(serverTCP.clientSocket.getInputStream());
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(serverTCP.clientSocket.getOutputStream())));
            String requestString="";
            while (true){
                if (serverTCP.clientSocket==null){
                    serverTCP.clientSocket = serverTCP.aSocket.accept();
                    in = new Scanner(serverTCP.clientSocket.getInputStream());
                    out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(serverTCP.clientSocket.getOutputStream())));
                }
                try{
                    requestString = in.nextLine();
                } catch(Exception e){
                    serverTCP.clientSocket.close();
                    serverTCP.clientSocket = null;
                    continue;
                }
                //System.out.println(requestString);
                String[] requests = requestString.split(",");
                serverTCP.init(requests);
                // check public key hash to ID
                if (!serverTCP.check())
                    break;
                // check if request is properly signed
                if (!serverTCP.decrypt(requests))
                    break;
                // perform the operation
                int result = 0;
                int value = 0;

                switch (serverTCP.option){
                    case 1:
                        value = Integer.parseInt(requests[4]);
                        result = serverTCP.addNum(serverTCP.id, value);
                        break;
                    case 2:
                        value = Integer.parseInt(requests[4]);
                        result = serverTCP.subNum(serverTCP.id, value);
                        break;
                    case 3:
                        result = serverTCP.hashMap.get(serverTCP.id);
                        break;
                    default:
                        System.out.println("invalid option input! Will return 0 for this option.");

                }
                System.out.println("Client ID: "+serverTCP.id+"; Operation Request: "+serverTCP.option+"; Returned Value: "+result);
                // send the result to the client
                out.println(result+"\n");
                out.flush();
            }

        } catch (IOException ex) {
            System.out.println("IO Exception: "+ex.getMessage());
        } catch (NoSuchAlgorithmException ex) {
            System.out.println("No such algorithm exception: "+ex.getMessage());;
        }

    }

}
