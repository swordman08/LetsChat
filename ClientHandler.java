import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;

/**
 * ClientHandler.java
 * Lets Client use usernames that weren't already used
 * Shows Client the list of usernames being used
 */
public class ClientHandler implements Runnable {
  private final Client client;
  private final ArrayList<Client> clientList;
  private final MtServer server;
  private boolean questionActive = false;
  private boolean correctAnswer;
  private boolean hostPresent = false;

  /**
   * Initializes a ClientHandler object.
   * 
   *
   * @param client     the client to handle
   *
   * @param clientList the list of clients
   *
   * @param server     the server
   */
  public ClientHandler(Client client, ArrayList<Client> clientList, MtServer server) {
    this.client = client;
    this.clientList = clientList;
    this.server = server;
  }

  /**
   * Runs the client handler thread.
   */
  public void run() {
    try {
      System.out.println("Connection made with socket " + client.getConnection());
      BufferedReader clientInput = 
          new BufferedReader(new InputStreamReader(client.getConnection().getInputStream()));
      DataOutputStream outputToClient =
          new DataOutputStream(client.getConnection().getOutputStream());

      String username;
      if (server.getHostAssigned() == false) {
        username = "Host";
        outputToClient.writeBytes("You are now the host of the game.\n");
        sendAvailableCommands(outputToClient);
        server.setHostAssigned();
      } else {
        do {
          outputToClient.writeBytes("Welcome! Please enter your username:\n");
          username = clientInput.readLine();
          if (!server.isUsernameAvailable(username)) {
            outputToClient.writeBytes("Username is already taken, please try another one:\n");
          } else {
            outputToClient.writeBytes("Your username has been accepted. \n");
            sendAvailableCommands(outputToClient);
          }
        } while (!server.isUsernameAvailable(username));
      }

      client.setUsername(username);
      server.addUsername(username);
      clientList.add(client);
      broadcastMessage(client.getUsername() + " has joined the chat");

      String clientText;
      while ((clientText = clientInput.readLine()) != null) {
        if (client.getUsername().equals("Host")) {
          switch (clientText.split(" ")[0]) {
            case "QUESTION":
              if (!questionActive) {
                String[] parts = clientText.split(" ", 3);
                if (parts.length < 3) {
                  return;
                }
                correctAnswer = Boolean.parseBoolean(parts[1]);
                broadcastMessage("Quiz Question: " + parts[2] + " \n");
                outputToClient.writeBytes("Quiz Question: " + parts[2] + " \n");
                System.out.println("Quiz Question: " + parts[2]);
                questionActive = true;
              }
              break;
            case "SCORES":
              StringBuilder scores = new StringBuilder("Current scores: \n");
              for (Client c : clientList) {
                scores.append(c.getUsername()).append(": ").append(c.getScore()).append("\n");
              }
              outputToClient.writeBytes(scores.toString());
              broadcastMessage(scores.toString());
              System.out.println(scores.toString());
              break;
            // Additional host commands

            case "ADDPOINTS":
              String[] points = clientText.split(" ", 3);
              addPointsToClient(points[1], Integer.parseInt(points[2]));
              break;

            default:
              break;
          }
        } else if (questionActive) {
          System.out.print(correctAnswer + "");
          // Check if the answer is correct
          if (clientText.equalsIgnoreCase("True") || clientText.equalsIgnoreCase("False")) {
            boolean answer = Boolean.parseBoolean(clientText);
            if (answer == correctAnswer) {
              client.addScore(10); // Assign points
              broadcastMessage(client.getUsername() + " answered correctly and scores 10 points!");
              questionActive = false; // Reset question state

            }
          } else {
            outputToClient.writeBytes("Incorrect answer. Try again!\n");
          }

        }

        if (questionActive == false) {  
          switch (clientText) {

            case "Goodbye":
              System.out.print(client.getUsername() + " has left the chat");
              broadcastMessage(client.getUsername() + " has left the chat ");
              server.removeUsername(client.getUsername());
              client.getConnection().close();
              clientList.remove(client);
              // outputToClient.close();
              // clientInput.close();
              return; // Exit run method and thus terminate thread

            case "Who?":
              StringBuilder users = new StringBuilder("Online users: ");
              for (Client c : clientList) {

                users.append(c.getUsername()).append(", ");
              }
              if (users.length() > 13) { // Remove trailing comma
                users.setLength(users.length() - 2);
              }
              outputToClient.writeBytes(users.toString() + "\n");
              System.out.println("" + users); // Debug
              break;
              

            // case "SCORES":
            // StringBuilder scores = new StringBuilder("Current scores: \n");
            // for (Client c : clientList) {
            // scores.append(c.getUsername()).append(":
            // ").append(c.getScore()).append("\n");
            // }
            // broadcastMessage(scores.toString());
            // break;

            default:
              System.out.println("Received from " + client.getUsername() + ": " + clientText);
              broadcastMessage(client.getUsername() + ": " + clientText);
              break;
          }
        }
      }
    } catch (Exception e) {
      System.out.println("Error: " + e.toString());
      try {
        server.removeUsername(client.getUsername());
        clientList.remove(client);
        client.getConnection().close();
      } catch (IOException ex) {
        System.out.println("Error closing connection: " + ex.getMessage());
      }
    }
  }

  private void broadcastMessage(String message) throws IOException {
    for (Client c : clientList) {
      if (!c.equals(client)) { // Do not send the message back to the sender
        DataOutputStream clientOutput = new DataOutputStream(c.getConnection().getOutputStream());
        clientOutput.writeBytes(message + "\n");
      }
    }
  }

  private void sendAvailableCommands(DataOutputStream out) throws IOException {
    StringBuilder commands = new StringBuilder();
    commands.append("Available commands:\n")
        .append("1. Who? - Lists all connected users\n")
        .append("2. Goodbye - Disconnect from server\n")
        .append("3. SCORES - Displays current scores (Host only)\n")
        .append("4. QUESTION <answer> <question> - Send a quiz question (Host only)\n")
        .append("5. ADDPOINTS name amountofpointstoadd (Host only) \n");
    out.writeBytes(commands.toString());
  }

  private void addPointsToClient(String username, int points) throws IOException {
    for (Client c : clientList) {
      if (c.getUsername().equalsIgnoreCase(username)) {
        c.addScore(points);
        broadcastMessage(username + " has been awarded " + points + " points!");
        System.out.println(username + " has been awarded " + points + " points!");
      }
    }
  }
}