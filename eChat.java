import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class eChat
{
    private static final Set<ClientHandler> clients = ConcurrentHashMap.newKeySet(); // Store connected clients
    private static int maxClients;

    public static void main(String[] args) {

        // Error message when starting up the server
        if (args.length != 2) {
            System.out.println("Usage: java TCPServer <server port> <max clients>");
            return;
        }

        int port = Integer.parseInt(args[0]);
        maxClients = Integer.parseInt(args[1]);

        ExecutorService threadPool = Executors.newFixedThreadPool(maxClients);

        // The parameters of ServerSocket are responsible for allowing multiple devices to connect to the server
        try (ServerSocket serverSocket = new ServerSocket(port, 50, InetAddress.getByName("0.0.0.0")))
        {
            System.out.println("Server started on port " + port + ". Waiting for clients...");


            /* OPTIONAL */
            // Open two terminals when running the application
            try { new ProcessBuilder("cmd", "/c", "start", "open_clients.bat").start(); }
            catch (IOException e) { System.out.println("Failed to open client windows."); }


            while (true)
            {
                Socket clientSocket = serverSocket.accept();

                // If the max clients limit is reached, reject the new client
                if (clients.size() >= maxClients)
                {
                    try (BufferedWriter output = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())))
                    {
                        output.write("The server is full! Please try again later.");
                        output.newLine();
                        output.flush();
                    }
                    clientSocket.close();
                    continue;
                }

                System.out.println("New client connected: " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());
                threadPool.execute(new ClientHandler(clientSocket));
            }
        }
        catch (IOException e) { e.printStackTrace(); }
        finally { threadPool.shutdown(); }
    }

    // Handles individual clients simultaneously connecting to one server
    static class ClientHandler implements Runnable
    {
        private Socket clientSocket;
        private String username;
        private BufferedWriter output;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run()
        {
            try (
                BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                BufferedWriter output = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()))
            ) {
                // Ask the user for their username
                this.output = output;
                output.write("Enter a username: ");
                output.flush();

                // Read username
                this.username = input.readLine();
                if (this.username == null || this.username.trim().isEmpty()) {
                    output.write("Invalid username. Connection closing.");
                    output.newLine();
                    output.flush();
                    return;
                }

                // Declare the new user
                System.out.println(username + " has joined the chat.");
                clients.add(this);
                broadcast("Server", username + " has joined the chat!");

                // Read messages from client
                String clientMessage;
                while ((clientMessage = input.readLine()) != null)
                {
                    // Typing "exit" quits the chat
                    if (clientMessage.equalsIgnoreCase("exit")) { break; }

                    // Handles sending messages
                    String formattedMessage = username + ": " + clientMessage;
                    System.out.println(formattedMessage);
                    broadcast(username, clientMessage);
                }

            }
            catch (IOException e) { e.printStackTrace(); }

            // Be sure to disconnect at the end
            finally { disconnectClient(); }
        }

        // Send message to all connected clients
        private void broadcast(String sender, String message) {
            synchronized (clients) {
                for (ClientHandler client : clients) {
                    try {
                        client.output.write(sender + ": " + message);
                        client.output.newLine();
                        client.output.flush();
                    }
                    catch (IOException e) { e.printStackTrace(); }
                }
            }
        }

        // Remove client from the list and notify others
        private void disconnectClient() {
            clients.remove(this);
            System.out.println(username + " has left the chat.");
            broadcast("Server", username + " has left the chat.");

            try { clientSocket.close(); }
            catch (IOException e) { e.printStackTrace(); }
        }
    }
}
