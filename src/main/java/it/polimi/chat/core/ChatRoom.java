package it.polimi.chat.core;

import java.util.UUID;

public class ChatRoom {
    private String roomId;
    private String uniqueId;
    private String multicastIp;
    private String creatorUserId;

    public ChatRoom(String roomId, String multicastIp, String creatorUserId) {
        this.roomId = roomId;
        this.uniqueId = UUID.randomUUID().toString();
        this.multicastIp = multicastIp;
        this.creatorUserId = creatorUserId;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getMulticastIp() {
        return multicastIp;
    }

    public String getCreatorUserId() {
        return creatorUserId;
    }

}