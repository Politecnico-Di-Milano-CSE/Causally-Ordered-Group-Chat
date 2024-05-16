package it.polimi.chat.dto.message;

public enum MessageType {
    logRequest,
    logResponse,
    userHeartbeat,
    roomHeartbeat,
    vectorHeartbeat,
    roomMessage
}
