package it.polimi.chat.dto.message;

public class deleteMessage extends MessageBase {

    private String roomId;

    public deleteMessage(String userID, String roomId) {
        super(userID, MessageType.deleteMessage);
        this.roomId = roomId;

    }

    public String getRoomId() {
        return roomId;
    }

}
