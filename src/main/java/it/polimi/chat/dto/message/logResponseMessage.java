package it.polimi.chat.dto.message;

import it.polimi.chat.dto.LoggedMessage;
import it.polimi.chat.dto.VectorClock;

import java.util.ArrayList;
import java.util.Map;

public class logResponseMessage extends MessageBase {
    private Map<String,ArrayList<LoggedMessage>> log;
    private String RoomId;
    private VectorClock clock;
    public logResponseMessage(String Userid, String RoomID,Map<String,ArrayList<LoggedMessage>>  log, VectorClock clock) {
        super(Userid, MessageType.logResponse);
        this.RoomId = RoomID;
        this.log = log;
        this.clock = clock;
    }
    public String getRoomid() {
        return RoomId;
    }
    public Map<String,ArrayList<LoggedMessage>>getLog() {
        return log;
    }
    public VectorClock getVectorClock() {
        return clock;
    }

}
