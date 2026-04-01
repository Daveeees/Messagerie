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

        // récupération du pseudo du client
        Scanner sc = new Scanner(System.in);
        System.out.print("Entrez votre pseudo : ");
        String pseudo = sc.nextLine();

        // ouverture d'une socket UDP pour communiquer avec le serveur
        DatagramSocket socket = new DatagramSocket(0);

        // instantiation de l'adresse et port du serveur
        InetAddress serverAddress = InetAddress.getByName("localhost");
        int serverMainPort = 9000;

        // création du paquet contenant le message de bienvenue du client
        String joinMsg = "JOIN:" + pseudo;
        byte[] joinBytes = joinMsg.getBytes();
        DatagramPacket clientPacketUDP= new DatagramPacket(joinBytes, joinBytes.length, serverAddress, serverMainPort);

        // envoi du message
        socket.send(clientPacketUDP);

        // création du paquet recevant la réponse du serveur
        byte[] buffer = new byte[1024];
        DatagramPacket reponse = new DatagramPacket(buffer, buffer.length);

        // réception de la réponse du serveur
        socket.receive(reponse);

        // transformation du message reçu en String
        String reponseStr = new String(reponse.getData(), 0, reponse.getLength());

        // si le message reçu ne contient pas le port attribué au client
        if (!reponseStr.startsWith("PORT:")) {
            //anormal: on ferme la socket et le thread
            System.err.println("Réponse anormale");
            socket.close();
            return;
        }
        // sinon print du port
        else{
            System.out.println(reponseStr);
        }

        // récupération du port dédié par le serveur au client
        int portDedie = Integer.parseInt(reponseStr.substring(5).trim());

        // création du thread qui écoutera les envois du serveur
        Thread threadEcoute = new Thread(new ClientChatUDP(socket));
        // setDaemon(true) pour que le thread se ferme si le thread actuel se ferme
        threadEcoute.setDaemon(true);
        // lancement du thread
        threadEcoute.start();

        // ce thread sert à récupérer les entrées au clavier et à les transmettre au serveur
        String ligne;
        while (true) {
            ligne = sc.nextLine();

            // si le client souhaite quitter le serveur
            if (ligne.equalsIgnoreCase("exit")) {
                //envoyer "EXIT"
                byte[] exitMsg = "EXIT".getBytes();
                socket.send(new DatagramPacket(exitMsg, exitMsg.length, serverAddress, portDedie));
                // sortir de la boucle
                break;
            }
            byte[] msg = ligne.getBytes();
            socket.send(new DatagramPacket(msg, msg.length, serverAddress, portDedie));
        }

        // fermer la socket
        socket.close();
    }

    @Override
    public void run() {
        // méthode qui sera executée par le thread d'écoute
        byte[] bufferEcoute = new byte[1024];
        try {
            // récéption en boucle des paquets envoyés par le serveur
            while (true) {
                DatagramPacket paquet = new DatagramPacket(bufferEcoute, bufferEcoute.length);
                socket.receive(paquet);
                String message = new String(paquet.getData(), 0, paquet.getLength());

                //afficher le message reçu du serveur dans la console
                System.out.println(message);
            }
        } catch (SocketException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}