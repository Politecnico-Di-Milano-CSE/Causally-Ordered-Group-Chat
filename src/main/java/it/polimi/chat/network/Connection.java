package it.polimi.chat.network;

import it.polimi.chat.core.ChatRoom;
import it.polimi.chat.core.RoomRegistry;
import it.polimi.chat.core.User;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;


public class Connection {
    // Constants for the multicast and broadcast ports
    private static final int MULTICAST_PORT = 1234;
    private static final int BROADCAST_PORT = 1235;

    // Index for the next multicast IP to use
    public static int nextIpIndex = 0;
    // Array of multicast IPs
    public static final String[] MULTICAST_IPS = {"224.0.0.1", "224.0.0.2", "224.0.0.3"};

    // Flag to indicate if the broadcast listener is running
    private volatile boolean isBroadcastListenerRunning;
    // Multicast and broadcast sockets
    private MulticastSocket multicastSocket;
    private MulticastSocket broadcastSocket;

    // Constructor
    public Connection() {
        try {
            // Initialize the multicast and broadcast sockets
            this.multicastSocket = new MulticastSocket(MULTICAST_PORT);
            this.broadcastSocket = new MulticastSocket(BROADCAST_PORT);
            // Enable broadcasting on the broadcast socket
            this.broadcastSocket.setBroadcast(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Set the broadcast listener as running
        this.isBroadcastListenerRunning = true;
    }

    // Method to stop the broadcast listener
    public void stopBroadcastListener() {
        this.isBroadcastListenerRunning = false;
        broadcastSocket.close();
    }

    // Method to send a multicast message
    public void sendMulticastMessage(String message, String multicastIp) {
        try {
            // Convert the message to bytes
            byte[] buffer = message.getBytes("UTF-8");
            // Get the multicast group address
            InetAddress group = InetAddress.getByName(multicastIp);
            // Create a datagram packet with the message, group address and port
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, MULTICAST_PORT);
            // Send the packet
            multicastSocket.send(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to send a broadcast message
    public void sendBroadcastMessage(String message) {
        try {
            // Convert the message to bytes
            byte[] buffer = message.getBytes();
            // Get the broadcast address
            InetAddress address = InetAddress.getByName("255.255.255.255");
            // Create a datagram packet with the message, broadcast address and port
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, BROADCAST_PORT);
            // Send the packet
            broadcastSocket.send(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to listen for multicast messages
    public void listenForMulticastMessages(ChatRoom room, User user, Node node) {
        new Thread(() -> {
            while (node.isRunning()) {
                try {
                    // Create a buffer for incoming data
                    byte[] buffer = new byte[1000];
                    // Create a datagram packet for incoming packets
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    // Receive a packet
                    multicastSocket.receive(packet);
                    // Convert the packet data to a string
                    String message = new String(packet.getData(), 0, packet.getLength(), "UTF-8");
                    // Print the message
                    System.out.println(user.getUsername() + ": " + message);
                } catch (SocketException e) {
                    if (node.isRunning()) {
                        e.printStackTrace();
                    }
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // Method to process a room creation message
    private void processRoomCreationMessage(String message, RoomRegistry roomRegistry, User user) {
        if (message.startsWith("ROOM_CREATED|")) {
            // Split the message into parts
            String[] parts = message.split("\\|");
            // Extract the room ID, multicast IP and creator user ID
            String roomId = parts[1];
            String multicastIp = parts[2];
            String creatorUserId = parts[3];

            // Create a new chat room
            ChatRoom room = new ChatRoom(roomId, multicastIp, creatorUserId);
            // Register the room
            roomRegistry.registerRoom(room);
            // Print a message
            System.out.println("New room created: " + roomId + " with multicast IP address: " + multicastIp + ", Created by: " + creatorUserId);
        } else {
            // Print the received broadcast message
            System.out.println("Broadcast message received: " + message);
        }
    }

    // Method to listen for broadcast messages
    public void listenForBroadcastMessages(RoomRegistry roomRegistry, User user, Node node) {
        new Thread(() -> {
            while (node.isRunning()) {
                try {
                    // Create a buffer for incoming data
                    byte[] buffer = new byte[1000];
                    // Create a datagram packet for incoming packets
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    // Receive a packet
                    broadcastSocket.receive(packet);
                    // Process the received message
                    String message = new String(packet.getData(), 0, packet.getLength(), "UTF-8");
                    processRoomCreationMessage(message, roomRegistry, user);
                } catch (SocketException e) {
                    if (node.isRunning()) {
                        e.printStackTrace();
                    }
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public MulticastSocket getMulticastSocket() {
        return multicastSocket;
    }

    public void closeMulticastSocket() {
        multicastSocket.close();
    }

}