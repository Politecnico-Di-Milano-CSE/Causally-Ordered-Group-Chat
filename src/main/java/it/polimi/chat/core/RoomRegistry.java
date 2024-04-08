package it.polimi.chat.core;

import java.util.HashMap;
import java.util.Map;

public class RoomRegistry {
    private Map<String, ChatRoom> rooms; // Map to store rooms

    // Constructor for the RoomRegistry class
    public RoomRegistry() {
        rooms = new HashMap<>(); // Initialize the map
    }

    // Method to get the rooms
    public Map<String, ChatRoom> getRooms() {
        return rooms;
    }

    // Method to register a room
    public void registerRoom(ChatRoom room) {
        rooms.put(room.getRoomId(), room); // Add the room to the map
    }

    // Method to get a room by its ID
    public ChatRoom getRoomById(String roomId) {
        return rooms.get(roomId); // Return the room with the given ID
    }
}

