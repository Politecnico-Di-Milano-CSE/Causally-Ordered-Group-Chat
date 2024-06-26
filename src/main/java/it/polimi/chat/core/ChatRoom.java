package it.polimi.chat.core;

import java.io.Serializable;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.collections4.BidiMap;

public class ChatRoom implements Serializable {
    private String roomId;
    private String uniqueId;
    private String multicastIp;
    private String creatorUserId;
    private BidiMap <String,String> participants;  //<userId,Username>

    public ChatRoom(String roomId, String multicastIp, String creatorUserId, BidiMap <String,String> participants) {
        this.roomId = roomId;
        this.uniqueId = UUID.randomUUID().toString();
        this.multicastIp = multicastIp;
        this.creatorUserId = creatorUserId;
        this.participants = participants;
    }
    public ChatRoom(ChatRoom chatRoom) {
        this.roomId = chatRoom.getRoomId();
        this.uniqueId = chatRoom.getUniqueId();
        this.multicastIp = chatRoom.getMulticastIp();
        this.creatorUserId = chatRoom.getCreatorUserId();
        this.participants = chatRoom.getParticipants();
        this.roomId = chatRoom.getRoomId();
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

    public BidiMap<String, String> getParticipants() {
        return participants;
    }
    public Set<String> getAllParticipantUsername() {
        return participants.values();
    }
    public Set<String> getParticipantUserId() {
        return participants.keySet();
    }
    public String getParticipantUserId(String username) {
        return participants.get(username);
    }
    public String getParticipantUsername(String userid) {
        return participants.get(userid);
    }
    public void addParticipant(String userId, String username ){
        this.participants.put(userId,username);
    }

}