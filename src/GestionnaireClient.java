import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;

public class GestionnaireClient implements Runnable {

    private ClientInfo _client;
    private DatagramSocket _socket;
    private ConcurrentHashMap<String,ClientInfo> _clients;

    public GestionnaireClient(ClientInfo client, DatagramSocket socket, ConcurrentHashMap<String,ClientInfo> clients) throws SocketException {
        _client = client;
        _socket = socket;
        _clients = clients;
    }

    // méthode de diffusion d'un message à tous les clients présents dans la concurrent hashMap
    public void envoyerATous(String message){

        //parcours de tous les clients dans la concurrent hashMap
        for(ClientInfo autreClient : _clients.values()){
            // transformation du message de String en bytes
            byte[] envoyees = message.getBytes();

            // récupération de l'IP du client et de son port
            InetAddress adrClient = autreClient.getAdresseIP();
            int prtClient = autreClient.getPort();

            // création d'un paquet UDP contenat le message à envoyer et le destinataire
            DatagramPacket paquetEnvoye = new DatagramPacket(envoyees, envoyees.length, adrClient, prtClient);

            // envoi du paquet UDP
            try {
                _socket.send(paquetEnvoye);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void run() {

        // diffuser le message de bienvenue à tous les clients
        String messageBienvenue = "JOIN: " + _client.getPseudo();
        envoyerATous(messageBienvenue);

        //  recevoir en boucle les messages du client via la socket dédiée
        try {
            while (true) {

                // création du paquet UDP qui recevra le message du client
                byte[] recues = new byte[1024];
                DatagramPacket paquetRecu = new DatagramPacket(recues, recues.length);

                // réception du paquet sur la socket deédiée
                _socket.receive(paquetRecu);

                // récupération du message reçu sous forme de String
                String messageRecu = new String(paquetRecu.getData(), 0, paquetRecu.getLength());

                // si le message recu par le client est "exit"
                if(messageRecu.equals("EXIT")){
                    // enlever le client de la concurrent hashMap
                    _clients.remove(_client.getPseudo());

                    //envoyer à tous les clients restants que ce client vient de quitter le serveur
                    String messageLeave = _client.getPseudo() + " a quitté le serveur";
                    envoyerATous(messageLeave);

                    //fermer la socket
                    _socket.close();

                    // sortir de la boucle
                    break;
                }
                // afficher la liste de toutes les personnes connectées au serveur
                else if(messageRecu.equals("/liste")){
                    for(String pseudo : _clients.keySet()){
                        // transformation du pseudo de String en byte[]
                        byte[] pseudoEnvoye = pseudo.getBytes();

                        // récupération de l'adresse IP et du port du client qui fait la demande de /liste
                        InetAddress adresseDemandeur = _client.getAdresseIP();
                        int portDemandeur = _client.getPort();

                        // création du packet UDP à envoyer avec le pseudo
                        DatagramPacket packetAEnvoyer = new DatagramPacket(pseudoEnvoye,pseudoEnvoye.length,adresseDemandeur,portDemandeur);

                        // envoi du packet
                        try {
                            _socket.send(packetAEnvoyer);
                        }
                        catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                // envoi de message privé
                else if(messageRecu.contains("/mp")){
                    // récupération du pseudo et du message du receveur
                    String[] parties = messageRecu.split(" ", 3);
                    String pseudoReceveur = parties[1];
                    String messageReceveur = parties[2];

                    ClientInfo clientReceveur = _clients.get(pseudoReceveur);
                    // si le client est présent dans la concurrent hashMap
                    if(clientReceveur != null){
                        // transformation du message de String en bytes[]
                        byte[] messageReceveurBytes = ("<MP> " + _client.getPseudo() +":" + messageReceveur).getBytes();

                        // récupération de l'adresse IP et du port du receveur
                        InetAddress adresseReceveur =  clientReceveur.getAdresseIP();
                        int portReceveur =  clientReceveur.getPort();

                        // création du paquet UDP contenant le message qui sera envoyé
                        DatagramPacket packetMessageUDP = new DatagramPacket(messageReceveurBytes,messageReceveurBytes.length,adresseReceveur,portReceveur);

                        // envoi du paquet
                        try{
                            _socket.send(packetMessageUDP);
                        }
                        catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    // si l'utilisateur n'est pas présent dans la concurrent hashMap
                    // envoyer au client envoyeur que l'utilisateur qu'il essaye de contacter est inexistant
                    else{
                        // message mis en bytes
                        byte[] pseudoEnvoye = "Utilisateur inexistant".getBytes();

                        // récupération de l'adresse IP et du port du client qui a créé le MP
                        InetAddress adresseDemandeur = _client.getAdresseIP();
                        int portDemandeur = _client.getPort();

                        // création du packet UDP à envoyer
                        DatagramPacket packetAEnvoyer = new DatagramPacket(pseudoEnvoye,pseudoEnvoye.length,adresseDemandeur,portDemandeur);

                        // envoi du packet
                        try {
                            _socket.send(packetAEnvoyer);
                        }
                        catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

                // si le message recu par le client n'est pas "exit"
                else{
                    // transmettre le message envoyer par le client à tout le monde
                    String messageATransmettre = _client.getPseudo() + ": " + messageRecu;
                    envoyerATous(messageATransmettre);
                }

            }
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
