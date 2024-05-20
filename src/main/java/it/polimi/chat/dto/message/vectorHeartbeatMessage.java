package it.polimi.chat.dto.message;

import it.polimi.chat.dto.VectorClock;

public class vectorHeartbeatMessage extends MessageBase {
    private String RoomId;
    private VectorClock clock;
    private Integer lastcheckpoint;

    public vectorHeartbeatMessage(String UserId, String RoomId, VectorClock clock, Integer lastcheckpoint) {
        super(UserId, MessageType.vectorHeartbeat);
        this.RoomId = RoomId;
        this.clock = clock;

    }
    public String getRoomId() {
        return RoomId;
    }
    public VectorClock getVectorClock() {
        return clock;
    }
    public Integer getLastcheckpoint(){
        return lastcheckpoint;
    }
}
