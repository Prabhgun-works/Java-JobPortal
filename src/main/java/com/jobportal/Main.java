package com.jobportal;

import com.jobportal.controller.UserController;

public class Main {
    public static void main(String[] args) {
        UserController userController = new UserController();

        userController.registerUser(1, "Alice", "alice@example.com");
        userController.registerUser(2, "Bob", "bob@example.com");

        System.out.println("\nAll users:");
        userController.showAllUsers();
    }
}