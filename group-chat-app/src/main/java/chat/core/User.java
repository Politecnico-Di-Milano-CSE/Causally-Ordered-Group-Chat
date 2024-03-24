package chat.core;

import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Set;

public class User {
    private String username;
    private Set<Room> rooms;

    public User(String username) {
        this.username = username;
    }

    public void createRoom(String roomId) {
        // Send a create room message to all nodes
        rooms.add(new Room(roomId));
        broadcastToNodes(new Message("create", roomId, this.username));
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
