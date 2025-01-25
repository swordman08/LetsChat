import java.net.Socket;

/**
 * Client.java
 * Contains a socket connection and username variable.
 * Provides methods to manage the client's username.
 */
public class Client {
    private Socket connection;
    private String username;
    private int score;

    // Constructor
    public Client(Socket sock, String username) {
        this.connection = sock;
        this.username = username;
        score = 0;
    }

    // Get the username of the client
    public String getUsername() {
        return this.username;
    }

    // Set the username of the client
    public void setUsername(String username) {
        this.username = username;
    }

    // Get the socket connection
    public Socket getConnection() {
        return this.connection;
    }

    // Method to update score
    public void addScore(int points) {
        this.score += points;
    }

    public int getScore() {
        return score;
    }
}