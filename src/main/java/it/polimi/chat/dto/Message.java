package it.polimi.chat.dto;

import java.io.Serializable;
// No changes needed, remove the unused import statement for java.util.Set

import it.polimi.chat.core.RoomRegistry;
import org.apache.commons.collections4.BidiMap;

public class Message implements Serializable {
    private int id = 0;
    private String userID;
    private String roomId;
    private String multicastIp;
    private String content;
    private VectorClock vectorClock;
    private BidiMap <String,String> participants;
    //contains the userids and username of the participants
    private RoomRegistry roomRegistry;

    public Message(String userID, String roomId, String multicastIp, String content, VectorClock vectorClock, BidiMap<String,String> participants, RoomRegistry roomRegistry) {
        this.id++;
        this.userID = userID;
        this.roomId = roomId;
        this.multicastIp = multicastIp;
        this.content = content;
        this.vectorClock = vectorClock;
        this.participants = participants;
        this.roomRegistry = roomRegistry;

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

    public BidiMap<String,String> getParticipants() {
        return participants;
    }

    public void setParticipants(BidiMap<String,String> participants) {
        this.participants = participants;
    }

    public RoomRegistry getRoomRegistry() {
        return roomRegistry;
    }

    public void setRoomRegistry(RoomRegistry roomRegistry) {
        this.roomRegistry = roomRegistry;
    }
}