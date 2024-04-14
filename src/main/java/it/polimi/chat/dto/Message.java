package it.polimi.chat.dto;

import java.io.Serializable;
import java.util.Set;

public class Message implements Serializable {
    private int id = 0;
    private String userID;
    private String roomId;
    private String multicastIp;
    private String content;
    private VectorClock vectorClock;
    private Set<String> participants;

    public Message(String userID, String roomId, String multicastIp, String content, VectorClock vectorClock, Set<String> participants) {
        this.id++;
        this.userID = userID;
        this.roomId = roomId;
        this.multicastIp = multicastIp;
        this.content = content;
        this.vectorClock = vectorClock;
        this.participants = participants;

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getMulticastIp() {
        return multicastIp;
    }

    public void setMulticastIp(String multicastIp) {
        this.multicastIp = multicastIp;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public VectorClock getVectorClock() {
        return vectorClock;
    }

    public void setVectorClock(VectorClock vectorClock) {
        this.vectorClock = vectorClock;
    }

    public Set<String> getParticipants() {
        return participants;
    }

    public void setParticipants(Set<String> participants) {
        this.participants = participants;
    }
}