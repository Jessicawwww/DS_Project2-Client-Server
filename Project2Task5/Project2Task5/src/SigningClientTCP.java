
import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.util.Random;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
/**
 * Client:
 * 1. create new RSA public and private keys and display these keys to the user.
 * 2. client ID: least significant 20 bytes of the hash of public key <e, n> and computed in the client code.
 * 3. transmit public key with each request.
 * 4. clients sign each request. with d and n, encrypt hash of the msg and add to each request.
 *   - here we use SHA-256 for h():
 *      - last20BytesOf(h(e+n))
 *      - E(h(all prior tokens),d)
 *      - signature is an encrypted hash using d and n < private key
 * 5. RSA-related refer to the link below.
 * @Reference: https://github.com/CMU-Heinz-95702/Project-2-Client-Server
 *
 */
public class SigningClientTCP {
    private int port = 7777;
    private InetAddress host;
    private Socket clientSocket;
    /*
    public key: (n, e); private key: (n. d)
    n: modulus for both private and public keys
    e: exponent of public key
    d: exponent of private key
     */
    private BigInteger n, e, d;
    private String publicKey, privateKey;
    private String id;
    Random rnd;
    public SigningClientTCP(int portNum) throws IOException {
        // RSA algorithm:
        rnd = new Random();
        // Step 1: Generate two large random primes.
        // We use 400 bits here, but best practice for security is 2048 bits.
        BigInteger p = new BigInteger(400, 100, rnd);
        BigInteger q = new BigInteger(400, 100, rnd);
        // Step 2: Compute n by the equation n = p * q.
        n = p.multiply(q);
        // Step 3: Compute phi(n) = (p-1) * (q-1)
        BigInteger phi = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));
        // Step 4: Select a small odd integer e that is relatively prime to phi(n).
        // By convention the prime 65537 is used as the public exponent.
        e = new BigInteger("65537");
        // Step 5: Compute d as the multiplicative inverse of e modulo phi(n).
        d = e.modInverse(phi);
        publicKey = e+","+n;
        privateKey = d+","+n;
        host = InetAddress.getByName("localhost");
        clientSocket = new Socket(host, port);
    }

    /**
     * create id: last20BytesOf(h(e+n))
     * @param en
     */
    public String setId(String en) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        // compute the digest with SHA-256
        byte[] bytesOfMessage = en.getBytes("UTF-8");
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] bigDigest = md.digest(bytesOfMessage);

        // we only want 20 bytes of the hash for ShortMessageSign
        // we add a 0 byte as the most significant byte to keep value to be signed non-negative.
        byte[] messageDigest = new byte[21];
        messageDigest[0] = 0;   // most significant set to 0
        // iterate to take bytes from SHA-256
        for (int i=0; i<20; i++){
            messageDigest[20-i] = bigDigest[bigDigest.length-1-i];
        }
        // From the digest, create a BigInteger
        BigInteger m = new BigInteger(messageDigest);
        id = m.toString();
        return m.toString();
    }

    /**
     * get operation option and number to operate on from user's input and return string to send
     * @param sc
     * @return string to send
     */
    public String start(Scanner sc){
        System.out.println("1. Add a value to your sum.\n" +
                "2. Subtract a value from your sum.\n" +
                "3. Get your sum.\n" +
                "4. Exit client");
        int option = sc.nextInt();
        int value=0;
        String valueString="";
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
        return id +","+ option +","+valueString;
    }

    /**
     * generate signature for requested message: encrypted hash using d and n(private key)
     * @param message
     * @return signature string
     * @throws UnsupportedEncodingException
     * @throws NoSuchAlgorithmException
     */
    public String getSignature(String message) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        // compute the digest with SHA-256
        byte[] bytesOfMessage = message.getBytes("UTF-8");
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] bigDigest = md.digest(bytesOfMessage);

        // we only want two bytes of the hash for ShortMessageSign
        // we add a 0 byte as the most significant byte to keep
        // the value to be signed non-negative.
        byte[] messageDigest = new byte[bigDigest.length+1];
        messageDigest[0] = 0;   // most significant set to 0
        // iterate to take bytes from SHA-256
        for (int i=0; i< bigDigest.length; i++){
            messageDigest[i+1] = bigDigest[i];
        }
        // From the digest, create a BigInteger
        BigInteger m = new BigInteger(messageDigest);
        // encrypt the digest with the private key
        BigInteger c = m.modPow(d, n);
        // return this as a big integer string
        return c.toString();
    }

    /**
     * use this method to send and receive message from or to server
     * @param message
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public void communicate(String message) throws IOException, NoSuchAlgorithmException {
        String signature = getSignature(message);
        String requestString = publicKey+","+message+","+signature;
        // send request to server
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())));
        out.println(requestString);
        out.flush();
        // receive response from server
        String replyString = in.readLine();
        System.out.println("The result is "+replyString+".");
    }

    /**
     * create a new client TCP to do proxy design.
     * @param args
     */
    public static void main(String[] args)  {
        System.out.println("The client is running.");
        Scanner sc = new Scanner(System.in);
        System.out.println("Please enter server port: ");
        int portNum = sc.nextInt();
        try {
            SigningClientTCP clientTCP = new SigningClientTCP(portNum);
            String en = String.valueOf(clientTCP.e)+String.valueOf(clientTCP.n);
            // generate client ID
            clientTCP.setId(en);
            // display public and private keys
            System.out.println("Public Key: "+"("+clientTCP.publicKey+")");
            System.out.println("Private Key: "+"("+clientTCP.privateKey+")");

            while (true){
                String operationString = clientTCP.start(sc);
                if (operationString.equals("halt")){
                    break;
                }
                // sign each request and send
                clientTCP.communicate(operationString);
            }
        } catch (IOException e){
            System.out.println("IO Exception: "+e.getMessage());
        } catch (NoSuchAlgorithmException ex) {
            System.out.println("No such algorithm exception: "+ex.getMessage());;
        }

    }

}
