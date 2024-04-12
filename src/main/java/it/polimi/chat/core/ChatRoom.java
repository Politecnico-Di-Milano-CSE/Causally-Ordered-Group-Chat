package it.polimi.chat.core;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class ChatRoom {
    private String roomId;
    private String uniqueId;
    private String multicastIp;
    private String creatorUserId;
    private Set<String> participants;
    boolean isContained = false;


    public ChatRoom(String roomId, String multicastIp, String creatorUserId, Set<String> participants) {
        this.roomId = roomId;
        this.uniqueId = UUID.randomUUID().toString();
        this.multicastIp = multicastIp;
        this.creatorUserId = creatorUserId;
        this.participants = participants;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getMulticastIp() {
        return multicastIp;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public String getCreatorUserId() {
        return creatorUserId;
    }

    public Set<String> getParticipants() {
        return participants;
    }

    public void addParticipant(String username){
        this.participants.add(username);
    }

}