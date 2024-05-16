package it.polimi.chat.dto.message;

import org.apache.commons.collections4.BidiMap;

public class roomHeartbeatMessage extends MessageBase {
    private String userID;
    private String roomId;
    private MessageType messageType;
    private String multicastIp;
    private BidiMap<String, String> participants;
    public roomHeartbeatMessage(String userid, String roomId, String multicastIp, BidiMap<String, String> participants) {
        this.userID = userid;
        this.roomId = roomId;
        this.multicastIp = multicastIp;
        this.participants = participants;
        this.messageType=MessageType.roomHeartbeat;
    }
    public String getUserID() {
        return userID;
    }
    public String getRoomId() {
        return roomId;
    }
    public MessageType getType() {
        return messageType;
    }
    public String getMulticastIp() {
        return multicastIp;
    }
    public BidiMap<String, String> getParticipants() {
        return participants;
    }
}
