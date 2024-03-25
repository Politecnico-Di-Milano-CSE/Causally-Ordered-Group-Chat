package chat.core;

import chat.util.CausalOrdering;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class User {
    private String username;
    private String userId;
    private Set<Room> rooms;
    private Map<String, Integer> vectorClock;
    private String serverAddress;
    private int serverPort;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public User(String username) {
        this.username = username;
        this.userId = UUID.randomUUID().toString();
        this.rooms = new HashSet<>();
        this.vectorClock = new HashMap<>();
        this.vectorClock.put(this.userId, 0);
    }

    public void setServerAddress(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    public void connectToServer() {
        try {
            socket = new Socket(serverAddress, serverPort);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            // Start a listener thread
            new Thread(this::listen).start();
        } catch (Exception e) {
            System.err.println("Error connecting to server: " + e.getMessage());
        }
    }

    private void listen() {
        try {
            while (socket.isConnected()) {
                Message message = (Message) in.readObject();
                handleIncomingMessage(message);
            }
        } catch (Exception e) {
            System.err.println("Error listening for messages: " + e.getMessage());
        }
    }

    private void handleIncomingMessage(Message message) {
        // Handle incoming messages here
        System.out.println("Received message: " + message);
    }

    public void createRoom(String roomId) {
        if (rooms.stream().noneMatch(room -> room.getRoomId().equals(roomId))) {
            Room newRoom = new Room(roomId);
            rooms.add(newRoom);
            incrementVectorClock();
            Message createMessage = new Message("create", roomId, this.username, new HashMap<>(vectorClock));
            sendMessage(createMessage);
        } else {
            System.out.println("Room already exists: " + roomId);
        }
    }

    public void deleteRoom(String roomId) {
        if (rooms.removeIf(room -> room.getRoomId().equals(roomId))) {
            incrementVectorClock();
            Message deleteMessage = new Message("delete", roomId, this.username, new HashMap<>(vectorClock));
            sendMessage(deleteMessage);
        } else {
            System.out.println("Room does not exist: " + roomId);
        }
    }

    private void sendMessage(Message message) {
        try {
            if (out != null) {
                out.writeObject(message);
                out.flush();
            } else {
                System.err.println("Error: Output stream is not initialized.");
            }
        } catch (IOException e) {
            System.err.println("Error sending message: " + e.getMessage());
        }
    }

    private void incrementVectorClock() {
        CausalOrdering.incrementClock(this.vectorClock, this.userId);
    }
}
