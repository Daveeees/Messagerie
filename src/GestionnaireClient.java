import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GestionnaireClient implements Runnable {

    private ClientInfo _client;
    private DatagramSocket _socket;
    private ConcurrentHashMap<String,ClientInfo> _clients;

    public GestionnaireClient(ClientInfo client, DatagramSocket socket, ConcurrentHashMap<String,ClientInfo> clients) {
        _client = client;
        _socket = socket;
        _clients = clients;
    }

    public void envoyerATous(String message){
        _clients.forEach((chaineCaractere, autreClient) -> {
            byte[] envoyees = message.getBytes();

            InetAddress adrClient = autreClient.getAdresseIP();
            int prtClient = autreClient.getPort();

            DatagramPacket paquetEnvoye = new DatagramPacket(envoyees, envoyees.length, adrClient, prtClient);

            try {
                _socket.send(paquetEnvoye);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void run() {

        // diffuser le message de bienvenue à tous les clients
        String messageBienvenue = "JOIN: " + _client.getPseudo();
        envoyerATous(messageBienvenue);

        //  recevoir en boucle les messages du client via la socket dédiée
        try {
            while (true) {

                byte[] recues = new byte[255];
                DatagramPacket paquetRecu = new DatagramPacket(recues, recues.length);
                _socket.receive(paquetRecu);
                String messageRecu = new String(paquetRecu.getData(), 0, paquetRecu.getLength());

                // traitement du EXIT
                if(messageRecu.equals("EXIT")){
                    _clients.remove(_client.getPseudo());
                    String messageLeave = _client.getPseudo() + " a quitté le serveur";
                    envoyerATous(messageLeave);
                    _socket.close();

                    break;
                }

                else{
                    String messageATransmettre = _client.getPseudo() + ": " + messageRecu;
                    envoyerATous(messageATransmettre);
                }

            }
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
