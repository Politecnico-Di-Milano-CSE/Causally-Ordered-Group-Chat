package it.polimi.chat.core;

import it.polimi.chat.network.Connection;
import it.polimi.chat.network.Node;

import java.util.*;

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
            System.out.println("6. Print list of known users");
            System.out.println("7. Print vector clock");
            System.out.println("8. Leave room");
            System.out.println("9. Print your UserId");
            System.out.println("10. Shut down the application");
            System.out.println("11. Print your log");
            System.out.print("Choose an option: ");

            int option = scanner.nextInt();
            scanner.nextLine(); // consume the newline

            switch (option) {
                case 1:
                    Set<String> participantUsernames = new HashSet<>();
                    String roomId;
                    Set<String> participantUIds = new HashSet<>();
                    do {
                        System.out.print("Enter the room ID: ");
                        roomId = scanner.nextLine();
                        System.out.print("Enter participant usernames (comma-separated): ");
                        String participantNamesInput = scanner.nextLine();
                        participantUsernames.addAll(Arrays.asList(participantNamesInput.split(",")));
                        if (participantUsernames.isEmpty()||participantUsernames.contains(user.getUsername())){
                            participantUsernames.clear();
                            System.out.print("You cant invite someone with your same username");
                        }
                    }while(participantUsernames.isEmpty());
                    for (String participant : participantUsernames) {
                        List<String> temp= (node.getConnection().getUsernameToId().get(participant));
                        if (temp.size()==1){
                            participantUIds.add(temp.get(0));
                        } else {
                            System.out.println("There is more than one UserIds associated to the Username, select which UID is the correct one:");
                            for (int i=0;i<temp.size();i++){
                                System.out.println(i+". "+temp.get(i));
                            }
                            int i=scanner.nextInt();
                            participantUIds.add(temp.get(i));

                        }
                    }
                    ChatRoom createdRoom = node.createRoom(roomId, participantUIds);
                    System.out.println("Room created with ID: " + createdRoom.getRoomId() + " and multicast IP address: "
                            + createdRoom.getMulticastIp() + ", Participants: " + participantUsernames);
                    break;
                case 2:
                    System.out.print("Enter the ID of the room to join: ");
                    String joinRoomId = scanner.nextLine();
                    ChatRoom roomToJoin = node.findRoomById(joinRoomId);
                    if (roomToJoin != null) {
                        if(node.getCurrentRoom() == null){
                            if (user.getRooms().contains(roomToJoin)) {
                                node.joinRoom(roomToJoin);
                            } else {
                                node.joinRoom(roomToJoin);
                            }
                        }
                        else {
                            System.out.println("You are already a member of the room with ID: " + node.getCurrentRoom().getRoomId());
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
                    node.getConnection().printKnownUsers();
                    break;
                case 7:
                    if(node.getVectorClock() == null){
                        System.out.println("You don't have a vector clock");
                        break;
                    }
                    node.printVectorclock();
                    break;
                case 8:
                    if (node.getCurrentRoom() == null) {
                        System.out.println("You are not in any room. Join a room before leaving it.");
                        break;
                    }
                    node.leaveRoom(node.getCurrentRoom());
                    break;
                case 9:
                    System.out.print("Here is your user id: "+user.getUserID());
                    break;
                case 10:
                    node.shutdown();
                    System.exit(0);
                case 11:
                    node.printYourLog();
            }
        }
    }
}