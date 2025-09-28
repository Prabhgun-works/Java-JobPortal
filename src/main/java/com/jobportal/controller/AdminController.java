package com.jobportal.controller;

import com.jobportal.service.JobApplicationService;
import com.jobportal.service.JobService;
import com.jobportal.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;
    private final JobService jobService;
    private final JobApplicationService applicationService;

    public AdminController(UserService userService,
                           JobService jobService,
                           JobApplicationService applicationService) {
        this.userService = userService;
        this.jobService = jobService;
        this.applicationService = applicationService;
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getAdminStats() {
        long userCount = userService.getAllUsers().size();
        long jobCount = jobService.getAllJobs().size();
        long applicationCount = applicationService.getAllApplications().size();

        return ResponseEntity.ok(Map.of(
                "users", userCount,
                "jobs", jobCount,
                "applications", applicationCount
        ));
    }
}
