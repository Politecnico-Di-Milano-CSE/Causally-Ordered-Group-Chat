package it.polimi.chat.dto.message;

import it.polimi.chat.dto.VectorClock;

public class logRequestMessage extends MessageBase{

    private Integer checkpoint;
    private String RoomId;
    private VectorClock vectorClock;
    public logRequestMessage(String Userid, String RoomID, Integer checkpoint, VectorClock vectorClock) {
        super(Userid, MessageType.logRequest);
        this.RoomId = RoomID;
        this.checkpoint = checkpoint;
        this.vectorClock = vectorClock;
    }
    public Integer getCheckpoint() {
        return checkpoint;
    }
    public String getRoomId() {
        return RoomId;
    }
    public VectorClock getVectorClock() {
        return vectorClock;
    }

}
