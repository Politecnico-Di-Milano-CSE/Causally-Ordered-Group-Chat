package it.polimi.chat.dto.message;

import it.polimi.chat.core.RoomRegistry;
import org.apache.commons.collections4.BidiMap;

public class registryHeartbeatMessage extends MessageBase {
    private RoomRegistry registry;
    private String username;
    public registryHeartbeatMessage(String userid,String username,RoomRegistry registry) {
        super(userid, MessageType.registryHeartbeat);
        this.registry = registry;
        this.username = username;
    }

public RoomRegistry getRegistry() {
    return registry;
    }
    public String getUsername() {
        return username;
    }
}
