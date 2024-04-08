package it.polimi.chat.network;

import it.polimi.chat.core.ChatRoom;
import it.polimi.chat.core.RoomRegistry;
import it.polimi.chat.core.User;

import java.net.InetAddress;

public class Node {
    // Instance variables
    private User user;  // The user associated with this node
    private Connection connection;  // The connection used by this node
    private RoomRegistry roomRegistry;  // The registry of rooms known to this node
    private ChatRoom currentRoom;  // The current room this node is in
    private boolean isRunning;  // Whether this node is running or not

    // Constructor
    public Node(User user) {
        this.user = user;  // Set the user
        this.connection = new Connection();  // Initialize the connection
        this.roomRegistry = new RoomRegistry();  // Initialize the room registry
        this.isRunning = true;  // Set the node as running

        // Start listening for multicast and broadcast messages
        connection.listenForMulticastMessages(this.currentRoom, this.user, this);
        connection.listenForBroadcastMessages(this.roomRegistry, this.user, this);
    }

    // Method to create a new room
    public ChatRoom createRoom(String roomId) {
        String multicastIp = getNextMulticastIp();  // Get the next available multicast IP
        ChatRoom room = new ChatRoom(roomId, multicastIp, user.getUserID());  // Create a new room
        joinRoom(room);  // Join the newly created room
        roomRegistry.registerRoom(room);  // Register the room in the room registry
        String announcement = "ROOM_CREATED|" + roomId + "|" + multicastIp + "|" + user.getUserID();  // Create an announcement
        connection.sendBroadcastMessage(announcement);  // Broadcast the announcement
        return room;  // Return the newly created room
    }

    // Method to delete a room
    public void deleteRoom(ChatRoom room) {
        leaveRoom(room);  // Leave the room
        user.getRooms().remove(room);  // Remove the room from the user's list of rooms
    }

    // Method to join a room
    public void joinRoom(ChatRoom room) {
        if (currentRoom != null) {
            leaveRoom(currentRoom);  // If already in a room, leave it
        }
        try {
            InetAddress group = InetAddress.getByName(room.getMulticastIp());  // Get the multicast group
            connection.getMulticastSocket().joinGroup(group);  // Join the multicast group
            currentRoom = room;  // Set the current room
            user.getRooms().add(room);  // Add the room to the user's list of rooms
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to leave a room
    public void leaveRoom(ChatRoom room) {
        try {
            InetAddress group = InetAddress.getByName(room.getMulticastIp());  // Get the multicast group
            connection.getMulticastSocket().leaveGroup(group);  // Leave the multicast group
            currentRoom = null;  // Set the current room to null
            System.out.println("You leaved the room " + room.getRoomId() + ".");  // Print a message
        } catch (Exception e) {
            e.printStackTrace();  // Print any exceptions
        }
    }

    // Method to send a message
    public void sendMessage(String message) {
        // Find the current room
        ChatRoom currentRoom = user.getRooms().stream()
                .filter(room -> room.getMulticastIp().equals(this.currentRoom != null ? this.currentRoom.getMulticastIp() : null))
                .findFirst()
                .orElse(null);

        if (currentRoom == null) {
            System.out.println("You are not in any room. Join a room before sending a message.");  // If not in a room, print a message
            return;
        }

        connection.sendMulticastMessage(message, currentRoom.getMulticastIp());  // Send the message
        System.out.println("Message sent to the room with ID: " + currentRoom.getRoomId());  // Print a message
    }

    // Method to print known rooms
    public void printKnownRooms() {
        System.out.println("List of known rooms:");

        System.out.println("Locally created rooms:");
        if (user.getRooms().isEmpty()) {
            System.out.println("No locally created rooms");
        } else {
            for (ChatRoom room : user.getRooms()) {
                System.out.println("- ID: " + room.getRoomId() + ", Multicast IP address: " + room.getMulticastIp() + ", Created by: " + room.getCreatorUserId());
            }
        }

        System.out.println("\nRooms received via broadcast:");
        if (roomRegistry.getRooms().isEmpty()) {
            System.out.println("No rooms received via broadcast");
        } else {
            for (ChatRoom room : roomRegistry.getRooms().values()) {
                System.out.println("- ID: " + room.getRoomId() + ", Multicast IP address: " + room.getMulticastIp() + ", Created by: " + room.getCreatorUserId());

            }
        }
    }

    // Method to find a room by ID
    public ChatRoom findRoomById(String roomId) {
        // Find the room in the user's list of rooms
        ChatRoom room = user.getRooms().stream()
                .filter(r -> r.getRoomId().equals(roomId))
                .findFirst()
                .orElse(null);

        if (room == null) {
            room = roomRegistry.getRoomById(roomId);  // If not found, find it in the room registry
        }

        return room;
    }

    private String getNextMulticastIp() {
        int index = Connection.nextIpIndex++ % Connection.MULTICAST_IPS.length;
        return Connection.MULTICAST_IPS[index];
    }

    public ChatRoom getCurrentRoom() {
        return currentRoom;
    }

    public void shutdown() {
        this.isRunning = false;

        for (ChatRoom room : user.getRooms()) {
            leaveRoom(room);
        }

        connection.closeMulticastSocket();
        connection.stopBroadcastListener();
    }

    public boolean isRunning() {
        return isRunning;
    }

}