package it.polimi.chat.storage;

import java.io.*;
import java.util.*;

import it.polimi.chat.core.RoomRegistry;
import it.polimi.chat.dto.Message;

public class PersistentStorage {
    private static final String ROOMS_FILE = "rooms.dat";
    private static final String MESSAGES_FILE = "messages.dat";

    public static void saveRoomData(RoomRegistry roomRegistry) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ROOMS_FILE))) {
            oos.writeObject(roomRegistry);
        }
    }

    public static RoomRegistry loadRoomData() throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(ROOMS_FILE))) {
            return (RoomRegistry) ois.readObject();
        }
    }

    @SuppressWarnings("unchecked")
    public static PriorityQueue<Message> loadMessages() throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(MESSAGES_FILE))) {
            return (PriorityQueue<Message>) ois.readObject();
        }
    }
}
