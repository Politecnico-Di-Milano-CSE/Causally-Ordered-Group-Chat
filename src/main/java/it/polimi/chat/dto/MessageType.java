package it.polimi.chat.dto;

public enum MessageType {
    logRequest,
    logResponse,
    userHeartbeat,
    roomHeartbeat,
    vectorHeartbeat,
    roomMessage
}
