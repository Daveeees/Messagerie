import java.io.IOException;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;

public class ServeurChatUDP {

    static void main() throws IOException {

        // création de la socket UDP principale sur laquelle les clients enverront leurs messages
        DatagramSocket socketUDPServeur = new DatagramSocket(null);
        InetSocketAddress adresse = new InetSocketAddress("localhost", 9000);
        socketUDPServeur.bind(adresse);

        //création de la concurrent hashMap qui contiendra tous les clients
        ConcurrentHashMap<String, ClientInfo> clients = new ConcurrentHashMap<String,ClientInfo>();

        // création du buffer qui contiendra les messages
        byte[] bufferMessage = new byte[1024];


        // écoute des clients en boucle
        while(true){

            // création du paquet UDP dans lequel seront reçus les messages
            DatagramPacket packetUDP = new DatagramPacket(bufferMessage,bufferMessage.length);

            // réception du paquet UDP sur la socket
            socketUDPServeur.receive(packetUDP);

            // transformation du message recu en string
            String message = new String(packetUDP.getData(), 0, packetUDP.getLength());

            // récupération de l'adresse IP et du port du client qui a envoyé le message
            InetAddress adresseClient = packetUDP.getAddress();
            int portClient = packetUDP.getPort();

            // si le client vient de rejoindre le chat
            if(message.contains("JOIN:")){
                // récupération du pseudo client
                String pseudo = message.substring(5).trim();

                // création du port dédié pour le client
                // le port est choisi automatiquement parmi ceux disponibles
                DatagramSocket socketClient = new DatagramSocket(0);

                // création du paquet UDP contenant le message avec le port dédié au client
                String messagePortCree = "PORT: " + socketClient.getLocalPort();
                byte[] bufferReception = messagePortCree.getBytes();
                DatagramPacket messageCreationPort = new DatagramPacket(bufferReception,bufferReception.length,adresseClient,portClient);

                // envoi du paquet avec le port dedié au client
                socketUDPServeur.send(messageCreationPort);

                // création du nouveau client
                ClientInfo client = new ClientInfo(pseudo, adresseClient, portClient);

                // mise du nouveau client dans la concurrent hashMap
                clients.put(pseudo, client);

                // création du gestionnaire client pour ce client
                GestionnaireClient gestionnaireClient = new GestionnaireClient(client, socketClient, clients);

                // lancement du gestionnaire client sur un nouveau thread
                Thread t = new Thread(gestionnaireClient);
                t.start();
           }
        }
    }
}
