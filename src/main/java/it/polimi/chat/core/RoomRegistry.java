package it.polimi.chat.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class RoomRegistry implements Serializable {
    private Map<String, ChatRoom> rooms; // Map to store rooms
    private HashSet<String> deletedRooms;
    // Constructor for the RoomRegistry class
    public RoomRegistry() {
        rooms = new HashMap<>(); // Initialize the map
        deletedRooms = new HashSet<>();
    }

    // Method to get the rooms
    public Map<String, ChatRoom> getRooms() {
        return rooms;
    }

    // Method to register a room
    public void registerRoom(ChatRoom room) {
        rooms.put(room.getRoomId(), room); // Add the room to the map
    }
    public HashSet<String> getDeletedRooms() {
        return deletedRooms;
    }
    public void addDeletedRoom(String room) {
        deletedRooms.add(room);
    }

    // Method to get a room by its ID
    public ChatRoom getRoomById(String roomId) {
        return rooms.get(roomId); // Return the room with the given ID
    }
    public ChatRoom getRoomByUId(String uid){
        for (ChatRoom room : rooms.values()) {
            if(room.getUniqueId().equals(uid)){
                return room;
            }
        }
        return null;
    }

    // Method to remove a room by its ID
    public void removeRoomById(String roomId) {
        rooms.remove(roomId); // Remove the room with the given ID
    }

}
