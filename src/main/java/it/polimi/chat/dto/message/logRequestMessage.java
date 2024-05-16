package it.polimi.chat.dto.message;

import it.polimi.chat.dto.VectorClock;

public class logRequestMessage extends MessageBase{
    private MessageType type;
    private Integer checkpoint;
    private String Userid;
    private String RoomId;
    private VectorClock vectorClock;
    public logRequestMessage(String Userid, String RoomID, Integer checkpoint, VectorClock vectorClock) {
        this.Userid = Userid;
        this.RoomId = RoomID;
        this.checkpoint = checkpoint;
        this.type=MessageType.logRequest;
    }
    public MessageType getType() {
        return type;
    }
    public Integer getCheckpoint() {
        return checkpoint;
    }
    public String getUserid() {
        return Userid;
    }
    public String getRoomId() {
        return RoomId;
    }
    public VectorClock getVectorClock() {
        return vectorClock;
    }

}
