package it.polimi.chat.dto.message;

import it.polimi.chat.dto.VectorClock;
import org.apache.commons.collections4.BidiMap;

public class RoomMessage extends MessageBase {
    private String roomId;
    private String multicastIp;
    private String content;
    private VectorClock vectorClock;
    private BidiMap<String,String> participants;
    private Integer lastCheckpoint;
    //contains the userids and username of the participants

    public RoomMessage(String userID, String roomId, String multicastIp, String content, VectorClock vectorClock, BidiMap<String,String> participants, Integer Lastcheckpoint) {
        super(userID ,MessageType.roomMessage);
        this.userID = userID;
        this.roomId = roomId;
        this.multicastIp = multicastIp;
        this.content = content;
        this.vectorClock = vectorClock;
        this.participants = participants;
        this.lastCheckpoint=Lastcheckpoint;
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

    public Integer getLastCheckpoint() {
        return lastCheckpoint;
    }
}


