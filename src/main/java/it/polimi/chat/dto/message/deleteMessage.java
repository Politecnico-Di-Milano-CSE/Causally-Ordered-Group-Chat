package it.polimi.chat.dto.message;

public class deleteMessage extends MessageBase {

    private String roomId;
    private String multicastIp;
    private String content;

    public deleteMessage(String userID, String roomId, String multicastIp, String content) {
        super(userID, MessageType.deleteMessage);
        this.roomId = roomId;
        this.multicastIp = multicastIp;
        this.content = content;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getMulticastIp() {
        return multicastIp;
    }

    public String getContent() {
        return content;
    }

}
