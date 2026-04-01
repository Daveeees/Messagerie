import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Scanner;
import java.io.IOException;

public class ClientChatUDP implements Runnable {


    static void main() throws IOException{

        // recuperer le pseudo de l'utilisateur
        Scanner sc = new Scanner(System.in);
        System.out.println("Entrez votre pseudo : ");
        String pseudo = sc.nextLine();

        try {
            DatagramSocket socketUDP = new DatagramSocket();

            InetAddress adresseClient = InetAddress.getByName("localhost");
            int portClient = 9000;

            String message = "JOIN:" + pseudo;
            byte[] messageBytes = message.getBytes();
            DatagramPacket packetAEnvoyer = new DatagramPacket(messageBytes, messageBytes.length, adresseClient, portClient);
            socketUDP.send(packetAEnvoyer);

            byte[] buffer = new byte[1024];
            DatagramPacket packetReponse = new DatagramPacket(buffer, buffer.length);
            socketUDP.receive(packetReponse);

            String reponse = new String(packetReponse.getData(), 0, packetReponse.getLength());
            if (!reponse.startsWith("PORT:")) {
                System.err.println("Reponse anormale");
                socketUDP.close();
                return;
            }
            int portAssigne = Integer.parseInt(reponse.substring(5).trim());
            System.out.println("Port: " + portAssigne);

            Thread t = new Thread();
            t.start();
        }catch (SocketException e) {
            System.err.println(e);
        }



    }

    @Override
    public void run() {
        try {
            byte[] bufferIncoming = new byte[1024];
            DatagramSocket socket = new DatagramSocket();
            while (true) {
                DatagramPacket packetIncomming = new DatagramPacket(bufferIncoming, bufferIncoming.length);
                try {
                    socket.receive(packetIncomming);
                }
                catch (IOException e) {
                    System.err.println(e);
                }
                String incoming = new String(packetIncomming.getData(), 0, packetIncomming.getLength());
                System.out.println("INCOMING: " + incoming);
            }
        }
        catch(SocketException e){
            System.err.println(e);
            }
        }

    }


