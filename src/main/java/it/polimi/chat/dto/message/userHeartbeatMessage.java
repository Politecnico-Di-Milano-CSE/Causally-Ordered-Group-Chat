package it.polimi.chat.dto.message;

public class userHeartbeatMessage  extends MessageBase{
 private String username;
 private String userId;
 private MessageType type;
 public userHeartbeatMessage(String userId, String username){
     this.username = username;
     this.userId = userId;
     this.type = MessageType.userHeartbeat;
 }
 public String getUsername() {

     return username;
 }
 public String getUserId() {
     return userId;
 }
 public MessageType getType() {

     return type;
 }
}
