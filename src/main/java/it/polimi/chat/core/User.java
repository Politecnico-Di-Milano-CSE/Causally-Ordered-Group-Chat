package it.polimi.chat.core;

import java.util.HashSet;
import java.util.UUID;

public class User {

    private String userID;
    private String username;
    private HashSet<ChatRoom> rooms;

    public User(String username) {
        this.userID = UUID.randomUUID().toString(); // Generates a unique user ID
        this.username = username;
        this.rooms = new HashSet<>(); // Initializes the set of rooms
    }

    // Method to get the user's ID
    public String getUserID() {
        return userID;
    }

    // Method to get the user's username
    public String getUsername() {
        return username;
    }

    // Method to get the set of rooms the user is in
    public HashSet<ChatRoom> getRooms() {
        return rooms;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setRooms(HashSet<ChatRoom> rooms) {
        this.rooms = rooms;
    }
}
