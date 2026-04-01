import java.net.*;


public class ClientInfo {

    private String _pseudo;
    private InetAddress _adresseIP;
    private int _port;
    private long _dernierMessage;

    public ClientInfo(String pseudo, InetAddress adresseIP, int port) {
        _pseudo = pseudo;
        _adresseIP = adresseIP;
        _port = port;
        _dernierMessage = System.currentTimeMillis();
    }

    public String getPseudo() {
        return _pseudo;
    }
    public InetAddress getAdresseIP() {
        return _adresseIP;
    }
    public int getPort() {
        return _port;
    }
    public long getTempsDernierMessage() {
        return _dernierMessage;
    }
    public void resetTempsDernierMessage() {
        _dernierMessage = System.currentTimeMillis();
    }
}
