package client.network;
public interface ServerListener {
    void onMessageReceived(String action,String[] params);
    void onDisconnected();
}
