package it.polimi.chat.dto.message;

import it.polimi.chat.dto.LoggedMessage;
import it.polimi.chat.dto.VectorClock;

import java.util.ArrayList;

public class logResponseMessage extends MessageBase {
    private MessageType type;
    private ArrayList <LoggedMessage> log;
    private Integer checkpoint;
    private String Userid;
    private String RoomId;
    private VectorClock clock;
    public logResponseMessage(String Userid, String RoomID, Integer checkpoint, ArrayList<LoggedMessage> log, VectorClock clock) {
        this.Userid = Userid;
        this.RoomId = RoomID;
        this.checkpoint = checkpoint;
        this.log = log;
        this.type=MessageType.logResponse;
        this.clock = clock;
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
    public String getRoomid() {
        return RoomId;
    }
    public ArrayList<LoggedMessage> getLog() {
        return log;
    }
    public VectorClock getVectorClock() {
        return clock;
    }

}
