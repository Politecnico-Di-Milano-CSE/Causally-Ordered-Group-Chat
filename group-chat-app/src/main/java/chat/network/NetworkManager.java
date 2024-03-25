package chat.network;

import chat.core.Message;
import chat.core.Room;
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
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public NetworkManager(int port) {
        this.port = port;
    }

    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (!serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();
                executorService.submit(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ClientHandler implements Runnable {
        private final Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
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
                // Further logic...
            } else {
                // Notify the attempt to create a duplicate room
                System.out.println("Attempted to create a room that already exists: " + roomId);
            }
        }

        private void deleteRoom(String roomId, String username) {
            if (rooms.containsKey(roomId)) {
                // Remove the room from the list of rooms
                rooms.remove(roomId);
                // Further logic for notifying participants or handling the room deletion
            } else {
                // Handle case where room doesn't exist
            }
        }

    }

}
