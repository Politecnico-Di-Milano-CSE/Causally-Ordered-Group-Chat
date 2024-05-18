package it.polimi.chat.dto.message;

import java.io.Serializable;

public class MessageBase implements Serializable {
    public String userID;
    private MessageType type;
    //contains the userids and username of the participants
    public MessageBase() {}
    public MessageBase(String userID, MessageType type) {
        this.userID = userID;
        this.type = type;
    }
    public String getUserID() {
        return userID;
    }
    public void setUserID(String userID) {
        this.userID = userID;
    }
    public MessageType getType() {
        return type;
    }
    public void setType(MessageType type) {
        this.type = type;
    }
}

