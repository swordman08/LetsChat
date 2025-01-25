import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * MtServer.java
 * Has a list for all clients.
 * Has a set to track active usernames.
 * Has a boolean variable to see if a host has been assigned.
 */
public class MtServer {
  private ArrayList<Client> clientList;
  private Set<String> activeUsernames; // To track active usernames
  private boolean hostAssigned;

  /**
    * MtServer default constructor.
    */
  public MtServer() {
    clientList = new ArrayList<>();
    activeUsernames = new HashSet<>();
    hostAssigned = false;
  }

  public synchronized boolean isUsernameAvailable(String username) {
    return !activeUsernames.contains(username);
  }

  public synchronized void addUsername(String username) {
    activeUsernames.add(username);
  }

  public synchronized void removeUsername(String username) {
    activeUsernames.remove(username);
  }

  public synchronized boolean getHostAssigned() {
    return hostAssigned;
  }

  public synchronized void setHostAssigned() {
    hostAssigned = true;
  }

  private void getConnection() {
    System.out.println("Waiting for client connections on port 9004.");
    try (ServerSocket serverSock = new ServerSocket(9004)) {
      while (true) {
        Socket connectionSock = serverSock.accept();
        Client client = new Client(connectionSock, "");
        ClientHandler handler = new ClientHandler(client, this.clientList, this);
        Thread theThread = new Thread(handler);
        theThread.start();
      }
    } catch (IOException e) {
      System.out.println(e.getMessage());
    }
  }

  public static void main(String[] args) {
    MtServer server = new MtServer();
    server.getConnection();
  }
}