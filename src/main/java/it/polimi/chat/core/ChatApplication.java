package it.polimi.chat.core;

import it.polimi.chat.network.Node;

import java.util.Scanner;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

public class ChatApplication {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your username: ");
        String username = scanner.nextLine();
        User user = new User(username);
        Node node = new Node(user);

        while (true) {
            System.out.println("1. Create a room");
            System.out.println("2. Join a room");
            System.out.println("3. Delete a room");
            System.out.println("4. Send a multicast message");
            System.out.println("5. Print list of known rooms");
            System.out.println("6. Leave room");
            System.out.println("7. Shut down the application");
            System.out.print("Choose an option: ");
            int option = scanner.nextInt();
            scanner.nextLine(); // consume the newline

            switch (option) {
                case 1:
                    System.out.print("Enter the room ID: ");
                    String roomId = scanner.nextLine();
                    // New code to get participants - for simplicity, assume user IDs are entered
                    // separated by commas
                    System.out.print("Enter participant user IDs (comma-separated): ");
                    String participantIdsInput = scanner.nextLine();
                    Set<String> participantIds = new HashSet<>(Arrays.asList(participantIdsInput.split(",")));
                    // Pass the participant IDs to the createRoom method
                    ChatRoom createdRoom = node.createRoom(roomId, participantIds);
                    System.out
                            .println("Room created with ID: " + createdRoom.getRoomId() + " and multicast IP address: "
                                    + createdRoom.getMulticastIp() + ", Participants: " + participantIds);
                    break;
                case 2:
                    System.out.print("Enter the ID of the room to join: ");
                    String joinRoomId = scanner.nextLine();
                    ChatRoom roomToJoin = node.findRoomById(joinRoomId);
                    if (roomToJoin != null) {
                        if (user.getRooms().contains(roomToJoin)) {
                            System.out.println("You are already a member of the room with ID: " + joinRoomId);
                        } else {
                            node.joinRoom(roomToJoin);
                            System.out.println("Joined the room with ID: " + joinRoomId);
                        }
                    } else {
                        System.out.println("Room with ID " + joinRoomId + " not found.");
                    }
                    break;
                case 3:
                    if (node.getCurrentRoom() == null) {
                        System.out.println("You are not in any room. Join a room before deleting it.");
                        break;
                    }
                    ChatRoom roomToDelete = node.getCurrentRoom();
                    node.deleteRoom(roomToDelete);
                    System.out.println("Room with ID: " + roomToDelete.getRoomId() + " deleted.");
                    break;
                case 4:
                    System.out.print("Enter the message to send: ");
                    String message = scanner.nextLine();
                    node.sendMessage(message);
                    break;
                case 5:
                    node.printKnownRooms();
                    break;
                case 6:
                    if (node.getCurrentRoom() == null) {
                        System.out.println("You are not in any room. Join a room before leaving it.");
                        break;
                    }
                    node.leaveRoom(node.getCurrentRoom());
                    break;
                case 7:
                    node.shutdown();
                    System.exit(0);
            }
        }
    }
}