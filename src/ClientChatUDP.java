import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Scanner;
import java.io.IOException;

public class ClientChatUDP implements Runnable {

    private final DatagramSocket socket;

    public ClientChatUDP(DatagramSocket socket) {
        this.socket = socket;
    }

    static void main() throws IOException {

        Scanner sc = new Scanner(System.in);
        System.out.print("Entrez votre pseudo : ");
        String pseudo = sc.nextLine();

        DatagramSocket socket = new DatagramSocket();
        InetAddress serverAddress = InetAddress.getByName("localhost");
        int serverMainPort = 9000;

        String joinMsg = "JOIN:" + pseudo;
        byte[] joinBytes = joinMsg.getBytes();
        socket.send(new DatagramPacket(joinBytes, joinBytes.length, serverAddress, serverMainPort));

        byte[] buffer = new byte[1024];
        DatagramPacket reponse = new DatagramPacket(buffer, buffer.length);
        socket.receive(reponse);
        String reponseStr = new String(reponse.getData(), 0, reponse.getLength());

        if (!reponseStr.startsWith("PORT:")) {
            System.err.println("Réponse anormale");
            socket.close();
            return;
        }

        int portDedie = Integer.parseInt(reponseStr.substring(5).trim());

        Thread threadEcoute = new Thread(new ClientChatUDP(socket));
        threadEcoute.setDaemon(true);
        threadEcoute.start();

        String ligne;
        while (true) {
            ligne = sc.nextLine();

            if (ligne.equalsIgnoreCase("exit")) {
                byte[] exitMsg = "EXIT".getBytes();
                socket.send(new DatagramPacket(exitMsg, exitMsg.length, serverAddress, portDedie));
                break;
            }

            byte[] msg = ligne.getBytes();
            socket.send(new DatagramPacket(msg, msg.length, serverAddress, portDedie));
        }

        socket.close();
    }

    @Override
    public void run() {
        byte[] bufferEcoute = new byte[1024];
        try {
            while (true) {
                DatagramPacket paquet = new DatagramPacket(bufferEcoute, bufferEcoute.length);
                socket.receive(paquet);
                String message = new String(paquet.getData(), 0, paquet.getLength());
                System.out.println(message);
            }
        } catch (SocketException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}