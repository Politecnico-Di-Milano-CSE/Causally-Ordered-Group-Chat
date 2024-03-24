package chat.core;

import java.io.Serializable;

public class Message implements Serializable {
    private String type;
    private String roomId;
    private String username;

    public Message(String type, String roomId, String username) {
        this.type = type;
        this.roomId = roomId;
        this.username = username;
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
}
