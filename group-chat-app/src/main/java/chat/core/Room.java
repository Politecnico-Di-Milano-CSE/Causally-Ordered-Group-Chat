package chat.core;

import java.util.UUID;

public class Room {
    private String roomId;
    private String uniqueId; // Unique ID for the room

    public Room(String roomId) {
        this.roomId = roomId;
        this.uniqueId = UUID.randomUUID().toString(); // Assigning a unique ID
    }

    public String getRoomId() {
        return roomId;
    }

    public String getUniqueId() {
        return uniqueId;
    }
}
