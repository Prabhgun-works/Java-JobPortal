package com.jobportal.controller;

import com.jobportal.exception.ResourceNotFound;
import com.jobportal.model.User;
import com.jobportal.service.JobService;
import com.jobportal.service.JobApplicationService;
import com.jobportal.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/admin") // ✅ base URL matches frontend
public class UserController {

    private final UserService userService;
    private final JobService jobService;
    private final JobApplicationService applicationService;

    public UserController(UserService userService,
                          JobService jobService,
                          JobApplicationService applicationService) {
        this.userService = userService;
        this.jobService = jobService;
        this.applicationService = applicationService;
    }

    // ✅ Get all users (Admin only)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    // ✅ Get user by ID (Admin only)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable int id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFound("User not found with id: " + id));
    }

    // ✅ Admin statistics: number of users, jobs, applications
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/statistics")
    public ResponseEntity<List<Statistics>> getStatistics() {
        List<Statistics> stats = new ArrayList<>();
        stats.add(new Statistics("Users", (long) userService.getAllUsers().size()));
        stats.add(new Statistics("Jobs", (long) jobService.getAllJobs().size()));
        stats.add(new Statistics("Applications", (long) applicationService.getAllApplications().size()));
        return ResponseEntity.ok(stats);
    }

    // DTO for statistics
    public static class Statistics {
        private String label;
        private Long value;

        public Statistics(String label, Long value) {
            this.label = label;
            this.value = value;
        }

        public String getLabel() { return label; }
        public Long getValue() { return value; }
    }
}
