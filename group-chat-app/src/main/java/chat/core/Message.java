package chat.core;

import java.io.Serializable;
import java.util.Map;

public class Message implements Serializable {
    private String type;
    private String roomId;
    private String username;
    private Map<String, Integer> vectorClock; // The vector clock for the message

    public Message(String type, String roomId, String username, Map<String, Integer> vectorClock) {
        this.type = type;
        this.roomId = roomId;
        this.username = username;
        this.vectorClock = vectorClock;
    }

    public String getType() {
        return type;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getUsername() {
        return username;
    }

    public Map<String, Integer> getVectorClock() {
        return vectorClock;
    }

    // Method to increment this user's clock for sending a message
    public void incrementVectorClock(String userId) {
        vectorClock.put(userId, vectorClock.getOrDefault(userId, 0) + 1);
    }
}
