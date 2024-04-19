package it.polimi.chat.network;

import it.polimi.chat.core.ChatRoom;
import it.polimi.chat.core.RoomRegistry;
import it.polimi.chat.core.User;
import it.polimi.chat.dto.Message;
import org.apache.commons.collections4.BidiMap;

import java.io.*;
import java.net.*;
import java.util.*;

public class Connection {
    // Constants for the multicast and broadcast ports
    private static final int MULTICAST_PORT = 1234;
    private static final int DATAGRAM_PORT = 1235;
    private static final String DATAGRAM_IP = "192.168.1.22";

    // Index for the next multicast IP to use
    public static int nextIpIndex = 0;
    // Array of multicast IPs
    public static final String[] MULTICAST_IPS = { "224.0.0.10", "224.0.0.11", "224.0.0.12" };

    // Flag to indicate if the broadcast listener is running
    private volatile boolean isDatagramListenerRunning;
    // Multicast and broadcast sockets
    private MulticastSocket multicastSocket;
    private DatagramSocket datagramSocket;
    private Map<String, User> knownUsers;
    //Maps username to all of the userIds known for that username
    private Map<String, List<String>> usernameToId;

    // Constructor
    public Connection() {
        try {
            // Initialize the multicast and broadcast sockets
            this.multicastSocket = new MulticastSocket(MULTICAST_PORT);
            this.datagramSocket = new DatagramSocket(new InetSocketAddress(DATAGRAM_IP, DATAGRAM_PORT));
            // Enable broadcasting on the broadcast socket
            this.datagramSocket.setBroadcast(true);
            this.isDatagramListenerRunning = false;
            this.knownUsers = new HashMap<>();
            this.usernameToId = new HashMap<>();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Set the broadcast listener as running
        this.isDatagramListenerRunning = true;
    }

    // Method to stop the broadcast listener
    public void stopBroadcastListener() {
        // Set the flag to false to stop the listener threads
        isDatagramListenerRunning = false;
        // Close the sockets to interrupt any blocking calls
        datagramSocket.close();
    }

    // Method to send a multicast message
    public void sendMulticastMessage(Message message, String multicastIp) {
        try {
            // Serialize the Message object to a byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(message);
            byte[] buffer = baos.toByteArray();

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
    public void sendDatagramMessage(Message message) {
        try {
            // Serialize the Message object to a byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(message);
            byte[] buffer = baos.toByteArray();

            // Get the broadcast address
            InetAddress address = InetAddress.getByName("192.168.1.255");

            // Create a datagram packet with the serialized object, broadcast address, and port
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, DATAGRAM_PORT);

            // Send the packet
            datagramSocket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to listen for multicast messages
    public void listenForMulticastMessages(User user, Node node) {
        new Thread(() -> {
            isDatagramListenerRunning = true;
            while (node.isRunning() && isDatagramListenerRunning) {
                try {
                    byte[] buffer = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    multicastSocket.receive(packet);

                    ByteArrayInputStream bais = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
                    ObjectInputStream ois = new ObjectInputStream(bais);
                    Message message = (Message) ois.readObject();

                    if (node.getCurrentRoom() != null && node.getCurrentRoom().getRoomId().equals(message.getRoomId())) {
                        // Check if the message is from the current user
                        if (!message.getUserID().equals(user.getUserID())) {
                            // Check if the received vector clock has a timestamp for a different process that is greater than the local timestamp
                            for (Map.Entry<String, Integer> entry : message.getVectorClock().getClock().entrySet()) {
                                if (!entry.getKey().equals(message.getUserID()) && entry.getValue() > node.getVectorClock().getClock().get(entry.getKey())) {
                                    // If so, hold the message and break the loop
                                    System.out.println("Holding message until the message from the initial process is received.");
                                    return;
                                }
                            }

                            // If the loop completes without finding a greater timestamp, update the vector clock and print the message
                            node.getVectorClock().updateClock(message.getVectorClock().getClock(), user.getUserID());
                            message.getVectorClock().printVectorClock(node.getCurrentRoom().getParticipants());
                        }
                        System.out.println(knownUsers.get(message.getUserID()).getUsername() + ": " + message.getContent());
                    }
                } catch (SocketException e) {
                    if (isDatagramListenerRunning && node.isRunning()) {
                        e.printStackTrace();
                    }
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            isDatagramListenerRunning = false;
        }).start();
    }


    // Method to process a room creation message
    private void processRoomCreationMessage(Message message, RoomRegistry roomRegistry, User user) {
        // Check if the message is a room creation message
        if (message.getRoomId() != null && message.getMulticastIp() != null && message.getUserID() != null) {
            // Extract the room details from the message
            String roomId = message.getRoomId();
            String multicastIp = message.getMulticastIp();
            String creatorUserId = message.getUserID();
            String content = message.getContent();
            BidiMap<String,String> participants = message.getParticipants();
            if(roomRegistry.getRoomById(roomId)==null){
                // Create a new chat room with participants
                ChatRoom room = new ChatRoom(roomId, multicastIp, creatorUserId, participants);
                // Register the room
                roomRegistry.registerRoom(room);
            }

            // Print a message
            System.out.println(content);
        } else {
            // Print the received broadcast message
            System.out.println("Broadcast message received: " + message.getContent());
        }
    }

    // Method to listen for broadcast messages
    public void listenForBroadcastMessages(RoomRegistry roomRegistry, User user, Node node) {
        new Thread(() -> {
            // Set the flag to true when the listener starts
            isDatagramListenerRunning = true;
            while (node.isRunning() && isDatagramListenerRunning) {
                try {
                    // Create a buffer for incoming data
                    byte[] buffer = new byte[1024];
                    // Create a datagram packet for incoming packets
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    // Receive a packet
                    datagramSocket.receive(packet);

                    // Deserialize the received object
                    ByteArrayInputStream bais = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
                    ObjectInputStream ois = new ObjectInputStream(bais);
                    Message message = (Message) ois.readObject();

                    // Process the message
                    if (message.getRoomId() == null && message.getMulticastIp() == null) {
                        // It's a heartbeat message, update the known users
                        updateKnownUser(message.getUserID(), message.getContent());
                    } else {
                        processRoomCreationMessage(message, roomRegistry, user);
                    }
                } catch (SocketException e) {
                    if (isDatagramListenerRunning && node.isRunning()) {
                        e.printStackTrace();
                    }
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // Set the flag to false when the listener ends
            isDatagramListenerRunning = false;
        }).start();
    }

    private void updateKnownUser(String userId, String username) {
        if (!knownUsers.containsKey(userId)) {
            User newUser = new User(username);
            newUser.setUserID(userId);
            knownUsers.put(userId, newUser);
            System.out.println("New user added to known users: " + username);
            //usernameToId is update once we see a new userid
            if (!usernameToId.containsKey(username)) {
                usernameToId.put(username, new ArrayList<>());
                usernameToId.get(username).add(userId);
            } else{
                usernameToId.get(username).add(userId);
            }
        } else {
            User existingUser = knownUsers.get(userId);
            if (!existingUser.getUsername().equals(username)) {
                existingUser.setUsername(username);
                System.out.println("Known user updated: " + username);
            }
        }
    }

    public void printKnownUsers() {
        System.out.println("List of known users:");
        for (User user : knownUsers.values()) {
            System.out.println("- Username: " + user.getUsername() + ", UserID: " + user.getUserID());
        }
    }

    public MulticastSocket getMulticastSocket() {
        return multicastSocket;
    }

    public void closeMulticastSocket() {
        multicastSocket.close();
    }

    public Map<String, User> getKnownUsers() {
        return knownUsers;
    }

    public void setKnownUsers(Map<String, User> knownUsers) {
        this.knownUsers = knownUsers;
    }
    public Map <String, List<String>> getUsernameToId() {
        return usernameToId;
    }
}