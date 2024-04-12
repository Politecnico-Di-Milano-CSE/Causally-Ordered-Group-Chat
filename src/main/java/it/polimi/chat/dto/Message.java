package it.polimi.chat.dto;

import java.io.Serializable;

public class Message implements Serializable {
    private int id = 0;
    private String userID;
    private String roomId;
    private String multicastIp;
    private String content;

    public Message(String userID, String roomId, String multicastIp, String content) {
        this.id++;
        this.userID = userID;
        this.roomId = roomId;
        this.multicastIp = multicastIp;
        this.content = content;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;

    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getMulticastIp() {
        return multicastIp;
    }

    public void setMulticastIp(String multicastIp) {
        this.multicastIp = multicastIp;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", userID=" + userID +
                ", roomId='" + roomId + '\'' +
                ", multicastIp=" + multicastIp +
                ", content='" + content + '\'' +
                '}';
    }
}