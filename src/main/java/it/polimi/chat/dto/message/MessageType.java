package it.polimi.chat.dto.message;

public enum MessageType {
    logRequest,
    logResponse,
    userHeartbeat,
    registryHeartbeat,
    vectorHeartbeat,
    roomMessage
}
