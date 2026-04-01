import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Scanner;

public class ClientChatUDP {


    static void main() throws SocketException {

        // recuperer le pseudo de l'utilisateur
        Scanner sc = new Scanner(System.in);
        System.out.println("Entrez votre pseudo : ");
        String pseudo = sc.nextLine();

        DatagramSocket socketUDP = new DatagramSocket(0);

        String message = "JOIN: " + pseudo;
        byte[] messageBytes = message.getBytes();
        DatagramPacket packetAEnvoyer = new DatagramPacket(messageBytes,messageBytes.length);

        

    }
}
