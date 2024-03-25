package chat.core;

import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class User {
    private String username;
    private String userId; // Unique ID for the user
    private Set<Room> rooms;

    public User(String username) {
        this.username = username;
        this.userId = UUID.randomUUID().toString(); // Assigning a unique ID
        this.rooms = new HashSet<>(); // Initializing the set of rooms
    }

    public String getUsername() {
        return username;
    }

    public String getUserId() {
        return userId;
    }

    public void createRoom(String roomId) {
        // Check if the room already exists
        if (rooms.stream().noneMatch(room -> room.getRoomId().equals(roomId))) {
            Room newRoom = new Room(roomId);
            rooms.add(newRoom); // Add new room if it doesn't exist
            broadcastToNodes(new Message("create", roomId, this.username));
        } else {
            // Handle room already exists case
            System.out.println("Room already exists: " + roomId);
        }
    }

    public void deleteRoom(String roomId) {
        // Send a delete room message to all nodes
        rooms.removeIf(room -> room.getRoomId().equals(roomId));
        broadcastToNodes(new Message("delete", roomId, this.username));
    }

    private void broadcastToNodes(Message message) {
        // Example IPs for the nodes in the network
        String[] nodeAddresses = { "127.0.0.1", "127.0.0.2", "127.0.0.3", "127.0.0.4", "127.0.0.5" };
        int port = 5000; // The common port that all nodes are listening on

        for (String address : nodeAddresses) {
            try (Socket socket = new Socket(address, port);
                    ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
                out.writeObject(message);
                out.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
