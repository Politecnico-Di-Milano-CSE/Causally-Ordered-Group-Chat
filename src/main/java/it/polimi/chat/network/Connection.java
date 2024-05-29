package it.polimi.chat.network;

import it.polimi.chat.core.ChatRoom;
import it.polimi.chat.core.RoomRegistry;
import it.polimi.chat.core.User;
import it.polimi.chat.dto.LoggedMessage;
import it.polimi.chat.dto.MessageQueue;
import it.polimi.chat.dto.message.*;
import org.apache.commons.collections4.BidiMap;

import java.io.*;
import java.net.*;
import java.util.*;

import static it.polimi.chat.dto.message.MessageType.registryHeartbeat;
import static it.polimi.chat.dto.message.MessageType.deleteMessage;
import static it.polimi.chat.dto.message.MessageType.userHeartbeat;

public class Connection {
    // Constants for the multicast and broadcast ports
    private static final int MULTICAST_PORT = 49152;
    private static final int DATAGRAM_PORT = 49153;

    // Flag to indicate if the broadcast listener is running
    private volatile boolean isDatagramListenerRunning;
    // Multicast and broadcast sockets
    private MulticastSocket multicastSocket;
    private DatagramSocket datagramSocket;
    private Map<String, User> knownUsers;
    //Maps username to all the userIds known for that username
    private Map<String, List<String>> usernameToId;
    private String localIpAddress;
    private String broadcastAddress;

    // Constructor
    public Connection() {
        try {
            // Initialize the multicast and broadcast sockets
            localIpAddress = getLocalIPAddress();
            this.broadcastAddress = getBroadcastAddress(localIpAddress);
            this.multicastSocket = new MulticastSocket(MULTICAST_PORT);
            this.datagramSocket = new DatagramSocket(new InetSocketAddress(localIpAddress, DATAGRAM_PORT));
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
    public void sendMulticastMessage(MessageBase message, String multicastIp) {
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
    public void sendDatagramMessage(MessageBase message) {
        try {
            // Serialize the Message object to a byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(message);
            byte[] buffer = baos.toByteArray();

            // Create a datagram packet with the serialized object, broadcast address, and port
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(broadcastAddress), DATAGRAM_PORT);

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
                    byte[] buffer = new byte[16384]; //65536
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    multicastSocket.receive(packet);

                    ByteArrayInputStream bais = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
                    ObjectInputStream ois = new ObjectInputStream(bais);
                    MessageBase msg = (MessageBase) ois.readObject();
                    MessageQueue currentRoomLog;
                    switch (msg.getType()) {
                            case roomMessage:
                                RoomMessage message = (RoomMessage) msg;
                                if (node.getCurrentRoom() != null && node.getCurrentRoom().getRoomId().equals(message.getRoomId())) {
                                    // Check if the message is from the current user
                                    boolean vectorclockIsUpdated = true;
                                    currentRoomLog = node.getMessageQueues(node.getCurrentRoom().getRoomId());
                                    // Check if the received vector clock has a timestamp for a different process that is greater than the local timestamp
                                    if (!msg.getUserID().equals(user.getUserID())) {
                                        for (Map.Entry<String, Integer> entry : message.getVectorClock().getClock().entrySet()) {
                                            if (!entry.getKey().equals(message.getUserID())) {
                                                if (entry.getValue() > node.getVectorClock().getClock().get(entry.getKey())) {
                                                    // If so, hold the message and break the loop
                                                    {
                                                        System.out.println("Holding message until the message from the initial process is received.");
                                                        vectorclockIsUpdated = false;
                                                        break;
                                                    }
                                                }
                                            }else if (entry.getValue() > node.getVectorClock().getClock().get(entry.getKey()) + 1) {
                                                    System.out.println("Holding message until the message from the initial process is received.");
                                                    vectorclockIsUpdated = false;
                                                    break;
                                                }

                                        }
                                    }
                                        if (vectorclockIsUpdated) {
                                            // If the loop completes without finding a greater timestamp, update the vector clock and print the message
                                            if (!msg.getUserID().equals(user.getUserID())) {
                                                node.getVectorClock().updateClock(message.getVectorClock().getClock(), user.getUserID());
                                                currentRoomLog.addMessageToLog(message);
                                                currentRoomLog.updateLocalClock(node.getVectorClock());
                                            }
                                            System.out.println(knownUsers.get(message.getUserID()).getUsername() + ": " + message.getContent());
                                        } else {
                                            logRequestMessage logrequest = new logRequestMessage(user.getUserID(), node.getCurrentRoom().getRoomId(), node.getVectorClock());
                                            sendMulticastMessage(logrequest, node.getCurrentRoom().getMulticastIp());
                                        }

                                }
                                break;
                            case logRequest:
                                if (!msg.getUserID().equals(user.getUserID())) {
                                    logRequestMessage request = (logRequestMessage) msg;
                                    System.out.println("logrequest received from " + knownUsers.get(request.getUserID()).getUsername() + "in room: " + request.getRoomId()); //todo remove this
                                    if (node.getCurrentRoom().getRoomId().equals(request.getRoomId())) {
                                        currentRoomLog= node.getMessageQueues(node.getCurrentRoom().getRoomId());
                                        Map <String, ArrayList< LoggedMessage >> trimmedLog=currentRoomLog.getTrimmedMessageLog(request.getVectorClock());
                                        Boolean emptyLog = true;
                                        for (Map.Entry<String, ArrayList< LoggedMessage >> entry : trimmedLog.entrySet()){
                                            System.out.println(entry.getValue().size());
                                            if (entry.getValue().size() > 0){
                                                emptyLog = false;
                                                break;
                                            }
                                        }
                                        if(!emptyLog){ //todo change this into not null get trimmedlog
                                            logResponseMessage Response = new logResponseMessage(user.getUserID(), node.getCurrentRoom().getRoomId(),trimmedLog, node.getVectorClock());
                                            System.out.println("i sent a log"); //todo remove
                                            sendMulticastMessage(Response, node.getCurrentRoom().getMulticastIp());
                                           // Instrumentation.getObjectSize(Response);
                                        }
                                    }
                                }
                                break;
                            case logResponse:
                                System.out.println("log response recieved"); //todo remove
                                if (!msg.getUserID().equals(user.getUserID())) {
                                    logResponseMessage response = (logResponseMessage) msg;
                                    System.out.println("response size :"); //todo remove
                                    if (response.getRoomid().equals(node.getCurrentRoom().getRoomId())) {
                                        currentRoomLog= node.getMessageQueues(node.getCurrentRoom().getRoomId());
                                        if (!node.getVectorClock().isClockLocallyUpdated(response.getVectorClock().getClock())) {
                                            System.out.println("updating clock from log response from " + knownUsers.get(response.getUserID()).getUsername()); //todo remove?
                                            currentRoomLog.updatelog(response.getLog());
                                            node.getVectorClock().updateClock(response.getVectorClock().getClock(), user.getUserID());
                                        }
                                    }
                                }
                                break;
                            case vectorHeartbeat:
                                if (!msg.getUserID().equals(user.getUserID())) {
                                    vectorHeartbeatMessage heartbeat = (vectorHeartbeatMessage) msg;
                                    if (heartbeat.getRoomId().equals(node.getCurrentRoom().getRoomId())) {
                                        if (!node.getVectorClock().isClockLocallyUpdated(heartbeat.getVectorClock().getClock())) {
                                            logRequestMessage logrequest = new logRequestMessage(user.getUserID(), node.getCurrentRoom().getRoomId(), node.getVectorClock());
                                            sendMulticastMessage(logrequest, node.getCurrentRoom().getMulticastIp());
                                            System.out.println("clock doesnt seem updated from heartbeat"); //todo remove
                                        }
                                    }
                                }
                                break;
                            default:
                                System.out.println("Default multicast message: "); //todo remove
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
    private void processRoomCreationMessage(registryHeartbeatMessage message, RoomRegistry roomRegistry, User user) {
        // Check if the message is a room creation message
            if (!message.getRegistry().getRooms().isEmpty()){
                // It's a room heartbeat message, update the known rooms
                for (ChatRoom room : message.getRegistry().getRooms().values()) {
                    if (!roomRegistry.getRooms().containsKey(room.getRoomId())){
                        String roomId = room.getRoomId();
                        String multicastIp = room.getMulticastIp();
                        String userId = message.getUserID();
                        BidiMap<String,String> participants = room.getParticipants();
                        for (String participantId : participants.keySet()) {
                            updateKnownUser(participantId, participants.get(participantId));
                        }
                        ChatRoom updatedRoom = new ChatRoom(roomId, multicastIp, userId, participants);
                        roomRegistry.registerRoom(updatedRoom);
                        System.out.println("New room updated: " + updatedRoom.getRoomId());
                    }
                }
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
                    byte[] buffer = new byte[4096];
                    // Create a datagram packet for incoming packets
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    // Receive a packet
                    datagramSocket.receive(packet);

                    // Deserialize the received object
                    ByteArrayInputStream bais = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
                    ObjectInputStream ois = new ObjectInputStream(bais);
                    MessageBase message = (MessageBase) ois.readObject();

                    // Process the message
                    if (message.getType()==registryHeartbeat) {
                        registryHeartbeatMessage msg = (registryHeartbeatMessage) message;
                        // It's a heartbeat message, update the known users
                        updateKnownUser(msg.getUserID(), msg.getUsername());
                        processRoomCreationMessage(msg,roomRegistry,user);
                    }
                    if (message.getType()==deleteMessage) {
                        deleteMessage deleteMsg = (deleteMessage) message;
                        // It's a delete message, leave and delete room for all
                        System.out.println(deleteMsg.getContent());
                        processDeleteRoom(roomRegistry, user, deleteMsg);
                    }
                } catch (SocketException e) {
                    if (isDatagramListenerRunning && node.isRunning()) {
                        e.printStackTrace();
                    }
                    break;

                } catch (EOFException e) {
                    System.err.println("Deserialization error: " + e.getMessage());
                    e.printStackTrace();
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

    public String generateMulticastIp(RoomRegistry roomRegistry) {
        InetAddress randomAddress = null;
        try {
            Random rand = new Random();
            byte[] randomIp = new byte[4];

            boolean found = false;
            while (!found) {
                // First three octets are fixed
                randomIp[0] = (byte) 224;
                randomIp[1] = (byte) 0;
                randomIp[2] = (byte) 0;

                // Randomize the last octet between 19 and 254 included
                randomIp[3] = (byte) (rand.nextInt(236) + 19);

                randomAddress = InetAddress.getByAddress(randomIp);

                // Verify if the IP is available
                if (isAddressAvailable(randomAddress, roomRegistry)) {
                    System.out.println("The multicast IP generated can be used: " + randomAddress.getHostAddress());
                    found = true;
                } else {
                    System.out.println("The multicast IP generated is already in use: " + randomAddress.getHostAddress());
                }
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        assert randomAddress != null;
        return randomAddress.getHostAddress();
    }

    private boolean isAddressAvailable(InetAddress address, RoomRegistry roomRegistry) {
        boolean isContained = true;
        for (ChatRoom room : roomRegistry.getRooms().values()) {
            if (room.getMulticastIp().equals(address.getHostAddress())) {
                isContained = false;
                break;
            }
        }
        return isContained;
    }

    public String getLocalIPAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (!networkInterface.isLoopback() && networkInterface.isUp() &&
                        !networkInterface.getDisplayName().contains("VMware") &&
                        !networkInterface.getDisplayName().contains("Ethernet") &&
                        !networkInterface.getDisplayName().contains("Box")) {

                    Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                    while (inetAddresses.hasMoreElements()) {
                        InetAddress inetAddress = inetAddresses.nextElement();
                        if (inetAddress.isSiteLocalAddress()) {
                            return inetAddress.getHostAddress();
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getBroadcastAddress(String localIPAddress) throws SocketException {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();
            if (!networkInterface.isLoopback() && networkInterface.isUp() &&
                    !networkInterface.getDisplayName().contains("VMware") &&
                    !networkInterface.getDisplayName().contains("Box")) {

                List<InterfaceAddress> interfaceAddresses = networkInterface.getInterfaceAddresses();
                for (InterfaceAddress interfaceAddress : interfaceAddresses) {
                    InetAddress address = interfaceAddress.getAddress();
                    if (address.getHostAddress().equals(localIPAddress)) {
                        int ipAddress = bytesToInt(address.getAddress());
                        int subnet = bytesToInt(interfaceAddress.getNetworkPrefixLength());
                        int broadcast = ipAddress | ~(~0 << (32 - subnet));
                        return intToIp(broadcast);
                    }
                }
            }
        }
        return null;
    }

    private int bytesToInt(Object obj) {
        if (obj instanceof byte[]) {
            int value = 0;
            for (byte b : (byte[]) obj) {
                value = (value << 8) + (b & 0xff);
            }
            return value;
        } else if (obj instanceof Short) {
            return (Short) obj & 0xFFFF;
        } else {
            throw new IllegalArgumentException("Unsupported type: " + obj.getClass().getName());
        }
    }

    private String intToIp(int value) {
        return ((value >> 24) & 0xFF) + "." +
                ((value >> 16) & 0xFF) + "." +
                ((value >> 8) & 0xFF) + "." +
                (value & 0xFF);
    }

    public void processDeleteRoom(RoomRegistry roomRegistry, User user, deleteMessage deleteMsg) {
        String roomId = deleteMsg.getRoomId();

        // Get the room from the registry
        ChatRoom room = roomRegistry.getRoomById(roomId);

        try {
            if (room != null) {
                InetAddress group = InetAddress.getByName(room.getMulticastIp());
                InetAddress addr = InetAddress.getByName(getLocalIPAddress());
                NetworkInterface networkInterface = NetworkInterface.getByInetAddress(addr);
                user.getRooms().remove(room);

                // Remove the room from the registry
                roomRegistry.removeRoomById(roomId);
                System.out.println("Room with ID " + roomId + " has been deleted.");
                multicastSocket.leaveGroup(new InetSocketAddress(group, MULTICAST_PORT), networkInterface);

                // Remove the room from the user's list of rooms

            } else {
                System.out.println("KEKEKEKEKEKEKEKEKKEKEEKKKKKKKKKKKKKKKKKKKKKKK");
                //System.out.println("Room with ID " + roomId + " not found.");
            }
        } catch (Exception e){
            e.printStackTrace();
            roomRegistry.removeRoomById(roomId);
            //System.out.println("Room with ID " + roomId + " not found.");
        }

    }


}