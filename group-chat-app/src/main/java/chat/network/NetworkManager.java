package chat.network;

import chat.core.Message;
import chat.core.Room;
import chat.util.CausalOrdering;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NetworkManager {
    private final int port;
    private final Map<String, Room> rooms = new HashMap<>();
    private final Map<String, Integer> localVectorClock = new HashMap<>(); // Local vector clock
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private final String[] nodeAddresses;

    public NetworkManager(int port, String[] nodeAddresses) {
        this.port = port;
        this.nodeAddresses = nodeAddresses; // Initialize the node addresses
    }

    private class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private Map<String, Integer> localVectorClock; // Handler's copy of the vector clock

        public ClientHandler(Socket socket, Map<String, Integer> localVectorClock) {
            this.clientSocket = socket;
            this.localVectorClock = new HashMap<>(localVectorClock); // Make a copy of the vector clock
        }

        @Override
        public void run() {
            try (ObjectInputStream objectInputStream = new ObjectInputStream(clientSocket.getInputStream());
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream())) {
                Object object = objectInputStream.readObject();
                if (object instanceof Message) {
                    handleMessage((Message) object);
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        private void handleMessage(Message message) {
            // Update local clock with the received message's vector clock
            CausalOrdering.updateClock(this.localVectorClock, message.getVectorClock(), message.getUsername());
            switch (message.getType()) {
                case "create":
                    createRoom(message.getRoomId(), message.getUsername());
                    break;
                case "delete":
                    deleteRoom(message.getRoomId(), message.getUsername());
                    break;
            }
        }

        private void createRoom(String roomId, String username) {
            // Logic to handle the creation of a room
            if (!rooms.containsKey(roomId)) {
                Room newRoom = new Room(roomId);
                rooms.put(roomId, newRoom);
                // Further logic
            } else {
                // Notify the attempt to create a duplicate room
                System.out.println("Attempted to create a room that already exists: " + roomId);
            }
        }

        private void broadcastToNodes(Message message) {
            for (String address : nodeAddresses) {
                executorService.submit(() -> {
                    try (Socket socket = new Socket(address, port);
                            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
                        out.writeObject(message);
                        out.flush();
                    } catch (IOException e) {
                        System.err.println("Error broadcasting to node at address " + address + ": " + e.getMessage());
                    }
                });
            }
        }

        private void deleteRoom(String roomId, String username) {
            if (rooms.containsKey(roomId)) {
                rooms.remove(roomId);
                // Broadcast a delete room message to all nodes, including the vector clock
                Message deleteMessage = new Message("delete", roomId, username, new HashMap<>(this.localVectorClock));
                broadcastToNodes(deleteMessage);
                // Further logic for notifying participants or handling the room deletion
            } else {
                // Handle case where room doesn't exist
                System.out.println("Attempted to delete a room that does not exist: " + roomId);
            }
        }
    }

    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (!serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();
                // Here we pass the localVectorClock to the ClientHandler
                executorService.submit(new ClientHandler(clientSocket, new HashMap<>(localVectorClock)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Assuming port 5000 is used for communication
        int port = 5000;

        // Example node addresses, replace with actual addresses
        String[] nodeAddresses = { "127.0.0.1", "127.0.0.2", "127.0.0.3", "127.0.0.4", "127.0.0.5" };

        NetworkManager networkManager = new NetworkManager(port, nodeAddresses);
        networkManager.startServer(); // Start the server loop
    }
}
