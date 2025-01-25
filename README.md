# Chatgame

* MtClient.java handles user keyboard input.
* ClientListener.java receives responses from the server and displays them
* MtServer.java listens for client connections and creates a ClientHandler for each new client
* ClientHandler.java receives messages from a client and relays it to the other clients.
* This program runs a game where one client, the host, asks the players questions. The first one to answer correctly gets a point.



# Commands 

* "Who?" - Lists all connected users
* "Goodbye" - Disconnect from server
* "SCORES" - Displays current scores (Host only)
* "QUESTION answer question" - Send a quiz question (Host only)
* "ADDPOINTS name amountofpointstoadd" (Host only)



## Build Instructions

* Compile all of the files: javac *java

## Execution Instructions

* Run MtServer.java in one terminal
* Run MtClient.java in as many other terminals as you'd like
