package com.jobportal.controller;

import com.jobportal.model.User;
import com.jobportal.service.UserService;

public class UserController {
    private UserService userService = new UserService();

    public void registerUser(int id, String name, String email) {
        User user = new User(id, name, email);
        userService.addUser(user);
        System.out.println("User registered: " + name);
    }

    public void showAllUsers() {
        for (User u : userService.getAllUsers()) {
            System.out.println(u.getName() + " - " + u.getEmail());
        }
    }
}