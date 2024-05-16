package it.polimi.chat.dto.message;

import it.polimi.chat.dto.VectorClock;

public class vectorHeartbeatMessage extends MessageBase {
    private String userId;
    private String RoomId;
    private VectorClock clock;
    private MessageType type;
    public vectorHeartbeatMessage(String userId, String RoomId, VectorClock clock) {
        this.userId = userId;
        this.RoomId = RoomId;
        this.clock = clock;
        this.type = MessageType.vectorHeartbeat;
    }
    public String getUserId() {
        return userId;
    }
    public String getRoomId() {
        return RoomId;
    }
    public VectorClock getVectorClock() {
        return clock;
    }
}
