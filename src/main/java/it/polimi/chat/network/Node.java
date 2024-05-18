package it.polimi.chat.network;

import it.polimi.chat.core.ChatRoom;
import it.polimi.chat.core.RoomRegistry;
import it.polimi.chat.core.User;
import it.polimi.chat.dto.LoggedMessage;
import it.polimi.chat.dto.MessageQueue;
import it.polimi.chat.dto.VectorClock;
import it.polimi.chat.dto.message.*;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.net.NetworkInterface;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Node {
    private User user; // The user associated with this node
    private Connection connection; // The connection used by this node
    private RoomRegistry roomRegistry; // The registry of rooms known to this node
    private ChatRoom currentRoom; // The current room this node is in
    private boolean isRunning; // Whether this node is running or not
    private static final int MULTICAST_PORT = 49152; // replace with your actual multicast port
    private ScheduledExecutorService scheduler, roomScheduler;
    private VectorClock vectorClock;
    private Map<String,MessageQueue> messageQueues;

    // Constructor
    public Node(User user) {
        this.user = user; // Set the user
        this.connection = new Connection(); // Initialize the connection
        this.roomRegistry = new RoomRegistry(); // Initialize the room registry
        this.isRunning = true; // Set the node as running
        messageQueues = new HashMap<String,MessageQueue>();
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.roomScheduler = Executors.newSingleThreadScheduledExecutor();

        // Start listening for multicast and broadcast messages
        connection.listenForMulticastMessages( this.user, this);
        connection.listenForBroadcastMessages(this.roomRegistry, this.user, this);

        startHeartbeatMessages();

    }

    private void startHeartbeatMessages() {
        scheduler.scheduleAtFixedRate(this::sendHeartbeatMessage, 5, 5, TimeUnit.SECONDS);
    }

    private void sendHeartbeatMessage() {
        registryHeartbeatMessage heartbeatMessage = new registryHeartbeatMessage(user.getUserID(), user.getUsername(),this.roomRegistry);
        connection.sendDatagramMessage(heartbeatMessage);
    }

    // Method to create a new room
    public ChatRoom createRoom(String roomId, Set<String> participantIds) {
        // Check if a user is already in a room
        if (currentRoom != null) {
            // If so, leave the current room
            leaveRoom(currentRoom);
        }
        String multicastIp = connection.generateMulticastIp(roomRegistry);
        BidiMap<String,String> usernameIds = new DualHashBidiMap<String,String>();
        vectorClock = new VectorClock(participantIds);
        for (String id: participantIds ){
            usernameIds.put(id, connection.getKnownUsers().get(id).getUsername());
        }
        ChatRoom room = new ChatRoom(roomId, multicastIp, user.getUserID(), usernameIds);
        // Create a new room with participants
        room.addParticipant(user.getUserID(), user.getUsername());
        joinRoom(room); // Join the newly created room
        roomRegistry.registerRoom(room); // Register the room in the room registry

        // Broadcast the creation of the room with participant list
       /* String announcement = "Room with ID -> " + room.getRoomId() + " and multicast IP -> " + multicastIp +
                " created by userId -> " + user.getUsername() + "\nwith participants -> "
                + String.join(",", room.getAllParticipantUsername());*/
        registryHeartbeatMessage message = new registryHeartbeatMessage(user.getUserID(), user.getUsername(),roomRegistry);
        System.out.println("Room created!");
        printVectorclock();

        connection.sendDatagramMessage(message); // Broadcast the announcement
        return room; // Return the newly created room
    }

    // Method to delete a room
    public void deleteRoom(ChatRoom room) {
        leaveRoom(room); // Leave the room
        user.getRooms().remove(room); // Remove the room from the user's list of rooms
    }

    // Method to join a room
    public void joinRoom(ChatRoom room) {
        if (currentRoom != null) {
            leaveRoom(currentRoom); // If already in a room, leave it
        }

        // Check if the user is a participant of the room
        if (!room.getParticipantUserId().contains(user.getUserID())) {
            System.out.println("You are not a participant in this room.");
            return;
        }

        try {
            InetAddress group = InetAddress.getByName(room.getMulticastIp());
            InetAddress addr = InetAddress.getByName(connection.getLocalIPAddress());
            NetworkInterface networkInterface = NetworkInterface.getByInetAddress(addr);
            connection.getMulticastSocket().joinGroup(new InetSocketAddress(group, MULTICAST_PORT), networkInterface);
            currentRoom = room; // Set the current room
            System.out.println("Joined the room with ID: " + currentRoom.getRoomId());
            user.getRooms().add(room); // Add the room to the user's list of rooms
            vectorClock = new VectorClock(room.getParticipantUserId());
            messageQueues.put(room.getRoomId(),new MessageQueue(room.getParticipants()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to leave a room
    public void leaveRoom(ChatRoom room) {
        try {
            InetAddress group = InetAddress.getByName(room.getMulticastIp());
            InetAddress addr = InetAddress.getByName(connection.getLocalIPAddress());
            NetworkInterface networkInterface = NetworkInterface.getByInetAddress(addr);
            connection.getMulticastSocket().leaveGroup(new InetSocketAddress(group, MULTICAST_PORT), networkInterface);

            currentRoom = null; // Set the current room to null
            roomScheduler.shutdown();
            System.out.println("You left the room " + room.getRoomId() + "."); // Print a message
        } catch (Exception e) {
            e.printStackTrace(); // Print any exceptions
        }
    }

    // Method to send a message
    public void sendMessage(String content) {
        // Find the current room
        ChatRoom currentRoom = user.getRooms().stream()
                .filter(room -> room.getMulticastIp()
                        .equals(this.currentRoom != null ? this.currentRoom.getMulticastIp() : null))
                .findFirst()
                .orElse(null);

        if (currentRoom == null) {
            System.out.println("You are not in any room. Join a room before sending a message.");
            return;
        }

        // Check if the user is a participant in the current room before sending a message
        if (currentRoom.getParticipantUserId().contains(user.getUserID())) {
            vectorClock.incrementLocalClock(user.getUserID());
            RoomMessage message = new RoomMessage(user.getUserID(), currentRoom.getRoomId(), currentRoom.getMulticastIp(),
                                        content, vectorClock, currentRoom.getParticipants());
            if (vectorClock.isClockLocallyUpdated(message.getVectorClock().getClock())) {
                connection.sendMulticastMessage(message, currentRoom.getMulticastIp());
                /*System.out.println("Message sent to the room with ID: " + currentRoom.getRoomId());
                printVectorclock(); todo remove*/
                messageQueues.get(currentRoom.getRoomId()).addMessageToLog(message);
            } else {
                System.out.println("Cannot send message: local clock is not updated correctly.");
            }
        } else {
            System.out.println("You are not a participant in this room.");
        }
    }

    // Method to print known rooms
    public void printKnownRooms() {
        System.out.println("List of known rooms:");

        System.out.println("Locally created rooms:");
        if (user.getRooms().isEmpty()) {
            System.out.println("No locally created rooms");
        } else {
            for (ChatRoom room : user.getRooms()) {
                System.out.println("- ID: " + room.getRoomId() + ", Multicast IP address: " + room.getMulticastIp()
                        + ", Created by: " + room.getCreatorUserId());
            }
        }

        System.out.println("\nRooms received via broadcast:");
        if (roomRegistry.getRooms().isEmpty()) {
            System.out.println("No rooms received via broadcast");
        } else {
            for (ChatRoom room : roomRegistry.getRooms().values()) {
                System.out.println("- ID: " + room.getRoomId() + ", Multicast IP address: " + room.getMulticastIp()
                        + ", Created by: " + room.getCreatorUserId());

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
            room = roomRegistry.getRoomById(roomId); // If not found, find it in the room registry
        }

        return room;
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
        scheduler.shutdown();
    }

    public boolean isRunning() {
        return isRunning;
    }

    public Connection getConnection() {
        return connection;
    }

    public VectorClock getVectorClock() {
        return vectorClock;
    }

    public void printVectorclock() {
        if (currentRoom != null) {
            vectorClock.printVectorClock(currentRoom.getParticipants());
        } else {
            System.out.println("You are not in any room.");
        }
    }

    public RoomRegistry getRoomRegistry() {
        return roomRegistry;
    }

    public MessageQueue getMessageQueues(String roomId) {
        return messageQueues.get(roomId);
    }
    public void printYourLog (){
        for(LoggedMessage msg : messageQueues.get(currentRoom.getRoomId()).getMessageLog()){
            System.out.println(currentRoom.getParticipantUsername(msg.userid)+": "+ msg.content);
        }
    }
}