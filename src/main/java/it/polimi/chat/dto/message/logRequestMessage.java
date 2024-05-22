package it.polimi.chat.dto.message;

import it.polimi.chat.dto.VectorClock;

public class logRequestMessage extends MessageBase{


    private String RoomId;
    private VectorClock vectorClock;
    public logRequestMessage(String Userid, String RoomID, VectorClock vectorClock) {
        super(Userid, MessageType.logRequest);
        this.RoomId = RoomID;
        this.vectorClock = vectorClock;
    }
    public String getRoomId() {
        return RoomId;
    }
    public VectorClock getVectorClock() {
        return vectorClock;
    }

}
