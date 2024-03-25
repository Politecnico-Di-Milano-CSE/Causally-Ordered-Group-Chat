package it.polimi.groupchat;

import chat.core.User;
import java.util.Scanner;

public class App {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter your username:");
        String username = scanner.nextLine();

        User user = new User(username);

        // Replace with the actual server address and port
        user.setServerAddress("127.0.0.1", 6000);

        // Example to create a room
        System.out.println("Creating a room 'testRoom1'.");
        user.createRoom("testRoom1");

        // Keep the client running to listen for incoming messages
        boolean running = true;
        while (running) {
            System.out.println("Enter command (create, delete, exit):");
            String command = scanner.nextLine();
            switch (command) {
                case "create":
                    System.out.println("Enter room name to create:");
                    String roomToCreate = scanner.nextLine();
                    user.createRoom(roomToCreate);
                    break;
                case "delete":
                    System.out.println("Enter room name to delete:");
                    String roomToDelete = scanner.nextLine();
                    user.deleteRoom(roomToDelete);
                    break;
                case "exit":
                    running = false;
                    break;
                default:
                    System.out.println("Unknown command.");
                    break;
            }
        }

        scanner.close();
        System.out.println("Client exited.");
    }
}
