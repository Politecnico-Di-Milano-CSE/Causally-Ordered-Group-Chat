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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadLocalRandom;

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
    private Boolean isupdated;
    private ScheduledExecutorService logscheduler;
    private User mainuser;
    private Node mainnode;
    private logResponseMessage mainresponse;
    private ScheduledExecutorService responsescheduler;
    private Boolean isLastMessageResponse;
    // Constructor
    public Connection() {
        try {
            // Initialize the multicast and broadcast sockets
            localIpAddress = getLocalIPAddress();
            this.isupdated = true;
            this.isLastMessageResponse=false;
            this.broadcastAddress = getBroadcastAddress(localIpAddress);
            this.multicastSocket = new MulticastSocket(MULTICAST_PORT);
            this.datagramSocket = new DatagramSocket(new InetSocketAddress(localIpAddress, DATAGRAM_PORT));
            // Enable broadcasting on the broadcast socket
            this.datagramSocket.setBroadcast(true);
            this.isDatagramListenerRunning = false;
            this.knownUsers = new HashMap<>();
            this.usernameToId = new HashMap<>();
            this.logscheduler = Executors.newSingleThreadScheduledExecutor();
            this.responsescheduler = Executors.newSingleThreadScheduledExecutor();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Set the broadcast listener as running
        this.isDatagramListenerRunning = true;
    }

    // Constructor
    public void simulateComeback() {
        try {
            // Initialize the multicast and broadcast sockets
            localIpAddress = getLocalIPAddress();
            this.broadcastAddress = getBroadcastAddress(localIpAddress);
            this.multicastSocket = new MulticastSocket(MULTICAST_PORT);
            this.datagramSocket = new DatagramSocket(new InetSocketAddress(localIpAddress, DATAGRAM_PORT));
            // Enable broadcasting on the broadcast socket
            this.datagramSocket.setBroadcast(true);
            this.isDatagramListenerRunning = false;
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
                    byte[] buffer = new byte[16384]; //65536, 16384
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    multicastSocket.receive(packet);
                    ByteArrayInputStream bais = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
                    ObjectInputStream ois = new ObjectInputStream(bais);
                    MessageBase msg = (MessageBase) ois.readObject();
                    MessageQueue currentRoomLog;

                    this.mainuser = user;
                    this.mainnode = node;

                    switch (msg.getType()) {
                            case roomMessage:
                                RoomMessage message = (RoomMessage) msg;
                                if (node.getCurrentRoom() != null && node.getCurrentRoom().getRoomId().equals(message.getRoomId())) {
                                    this.isLastMessageResponse=false;
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
                                            if(isupdated){
                                                isupdated = false;
                                                try {
                                                    logscheduler.scheduleAtFixedRate(this::requestLogs, 5, 5, TimeUnit.SECONDS); //repeat the request until a logresponse is received
                                                }catch (Exception e){}
                                            }
                                        }

                                }
                                break;
                            case logRequest:
                                if (!msg.getUserID().equals(user.getUserID())) {
                                    logRequestMessage request = (logRequestMessage) msg;
                                    if (node.getCurrentRoom().getRoomId().equals(request.getRoomId())) {
                                           this.isLastMessageResponse=false;
                                        currentRoomLog= node.getMessageQueues(node.getCurrentRoom().getRoomId());
                                        Map <String, ArrayList< LoggedMessage >> trimmedLog=currentRoomLog.getTrimmedMessageLog(request.getVectorClock());
                                        Boolean emptyLog = true;
                                        for (Map.Entry<String, ArrayList< LoggedMessage >> entry : trimmedLog.entrySet()){
                                            if (entry.getValue().size() > 0){
                                                emptyLog = false;
                                                break;
                                            }
                                        }
                                        if(!emptyLog){
                                            this.mainresponse = new logResponseMessage(user.getUserID(), node.getCurrentRoom().getRoomId(),trimmedLog, node.getVectorClock());
                                            try {
                                                this.responsescheduler.schedule(this::respondlog,ThreadLocalRandom.current().nextInt(0,1000), TimeUnit.MILLISECONDS); //sends the response after a random time delay
                                            } catch (Exception e){}
                                        }
                                    }
                                }
                                break;
                            case logResponse:
                                if (!msg.getUserID().equals(user.getUserID())) {
                                    this.logscheduler.shutdown();
                                    logResponseMessage response = (logResponseMessage) msg;
                                    if (response.getRoomid().equals(node.getCurrentRoom().getRoomId())) {
                                        isLastMessageResponse=true;
                                        currentRoomLog= node.getMessageQueues(node.getCurrentRoom().getRoomId());
                                        if (!node.getVectorClock().isClockLocallyUpdated(response.getVectorClock().getClock())) {
                                            isupdated=true;
                                            System.out.println("updating log from from " + knownUsers.get(response.getUserID()).getUsername());
                                            currentRoomLog.updatelog(response.getLog());
                                            node.getVectorClock().updateClock(response.getVectorClock().getClock(), user.getUserID());
                                        }
                                    }
                                }
                                break;
                            default:
                                System.out.println("Default multicast message: ");
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
                    if (!roomRegistry.getRooms().containsKey(room.getRoomId()) && !roomRegistry.getDeletedRooms().contains(room.getUniqueId())){
                        BidiMap<String,String> participants = room.getParticipants();
                        for (String participantId : participants.keySet()) {
                            updateKnownUser(participantId, participants.get(participantId));
                        }
                        ChatRoom updatedRoom = new ChatRoom(room);
                        roomRegistry.registerRoom(updatedRoom);
                        System.out.println("New room updated: " + updatedRoom.getRoomId());
                    }
                }
            }
            if (!message.getRegistry().getDeletedRooms().isEmpty()){ //checks the list of deleted rooms and deletes any new rooms added to the deleted list
                for (String roomId : message.getRegistry().getDeletedRooms()) {
                    if(!roomRegistry.getDeletedRooms().contains(roomId)){
                        roomRegistry.addDeletedRoom(roomId);
                        ChatRoom deletedRoom =roomRegistry.getRoomByUId(roomId);
                        if(deletedRoom !=null){
                            processDeleteRoom(roomRegistry ,user,deletedRoom.getRoomId());
                        }
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
                        String roomId = deleteMsg.getRoomId();
                        processDeleteRoom(roomRegistry, user,roomId);
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
public void requestLogs(){
        if(!this.isupdated){
            logRequestMessage logrequest = new logRequestMessage(this.mainuser.getUserID(), this.mainnode.getCurrentRoom().getRoomId(), this.mainnode.getVectorClock());
            sendMulticastMessage(logrequest, mainnode.getCurrentRoom().getMulticastIp());
        } else{
            logscheduler.shutdown();
        }
}
public void shutdownscheduler(){ //shuts down the threads related to repeating requests and response
            logscheduler.shutdown();
            responsescheduler.shutdown();

}
public void respondlog(){
        if (!this.isLastMessageResponse) {
            sendMulticastMessage(this.mainresponse, this.mainnode.getCurrentRoom().getMulticastIp());
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

    public void closeBroadcastScoket() {
        datagramSocket.close();
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

    public void processDeleteRoom(RoomRegistry roomRegistry, User user, String roomId) {

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
                mainnode.removelogs(roomId);
                mainnode.leaveRoom(room);
                System.out.println("Room with ID " + roomId + " has been deleted.");
                multicastSocket.leaveGroup(new InetSocketAddress(group, MULTICAST_PORT), networkInterface);
                // Remove the room from the user's list of rooms

            }
        } catch (Exception e){
            mainnode.removelogs(roomId);
            roomRegistry.removeRoomById(roomId);
            }

    }


}