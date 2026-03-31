import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class ServeurChatUDP {

    static void main() throws IOException {

        DatagramSocket socketUDP = new DatagramSocket(null);

        InetSocketAddress adresse = new InetSocketAddress("localhost", 9000);

        socketUDP.bind(adresse);

        ConcurrentHashMap<String, ClientInfo> clients = new ConcurrentHashMap<String,ClientInfo>();

        byte[] bufferMessage = new byte[1024];
        while(true){
            DatagramPacket packetUDP = new DatagramPacket(bufferMessage,bufferMessage.length);
            socketUDP.receive(packetUDP);

            String message = new String(packetUDP.getData(), 0, packetUDP.getLength());

            InetAddress adresseClient = packetUDP.getAddress();
            int portClient = packetUDP.getPort();

            if(message.contains("JOIN:")){

                String pseudo = message.substring(5).trim();

                DatagramSocket socketClient = new DatagramSocket(0);

                String messagePortCree = "PORT: " + socketClient.getLocalPort();

                byte[] bufferReception = messagePortCree.getBytes();

                DatagramPacket messageCreationPort = new DatagramPacket(bufferReception,bufferReception.length,adresseClient,portClient);

                socketUDP.send(messageCreationPort);

                ClientInfo client = new ClientInfo(pseudo, adresseClient, portClient);

                clients.put(pseudo, client);

                GestionnaireClient gestionnaireClient = new GestionnaireClient(client, socketClient, clients);

                Thread t = new Thread(gestionnaireClient);
                t.start();
           }
        }
    }
}
