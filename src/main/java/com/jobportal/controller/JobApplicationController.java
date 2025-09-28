package com.jobportal.controller;

import com.jobportal.model.JobApplication;
import com.jobportal.model.User;
import com.jobportal.service.JobApplicationService;
import com.jobportal.service.JobService;
import com.jobportal.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api") // ✅ base URL matches frontend
public class JobApplicationController {

    private final JobApplicationService applicationService;
    private final JobService jobService;
    private final UserService userService;

    public JobApplicationController(JobApplicationService applicationService,
                                    JobService jobService,
                                    UserService userService) {
        this.applicationService = applicationService;
        this.jobService = jobService;
        this.userService = userService;
    }

    // ✅ Candidate applies for a job with resume
    @PreAuthorize("hasRole('CANDIDATE')")
    @PostMapping("/candidate/apply/{jobId}")
    public ResponseEntity<?> applyForJob(@PathVariable int jobId,
                                         @RequestHeader("Authorization") String token,
                                         @RequestParam("resume") MultipartFile resume) throws IOException {

        String email = userService.getEmailFromToken(token);
        User candidate = userService.getUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("Candidate not found"));

        JobApplication application = applicationService.applyToJob(candidate, jobId, resume);
        return ResponseEntity.ok(application);
    }

    // ✅ Candidate views their applications
    @PreAuthorize("hasRole('CANDIDATE')")
    @GetMapping("/candidate/applications")
    public ResponseEntity<List<JobApplication>> getMyApplications(@RequestHeader("Authorization") String token) {
        String email = userService.getEmailFromToken(token);
        User candidate = userService.getUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("Candidate not found"));

        return ResponseEntity.ok(applicationService.getApplicationsByCandidate(candidate));
    }

    // ✅ Employer views all applications for their jobs
    @PreAuthorize("hasRole('EMPLOYER')")
    @GetMapping("/employer/applications")
    public ResponseEntity<List<JobApplication>> getEmployerApplications(@RequestHeader("Authorization") String token) {
        String email = userService.getEmailFromToken(token);
        User employer = userService.getUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employer not found"));

        return ResponseEntity.ok(applicationService.getApplicationsForEmployer(employer));
    }

    // ✅ Admin views all applications
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/applications")
    public ResponseEntity<List<JobApplication>> getAllApplications() {
        return ResponseEntity.ok(applicationService.getAllApplications());
    }

    // ✅ Employer/Admin updates application status (ACCEPTED/REJECTED)
    @PreAuthorize("hasRole('EMPLOYER') or hasRole('ADMIN')")
    @PutMapping("/applications/{applicationId}/status")
    public ResponseEntity<?> updateApplicationStatus(@PathVariable int applicationId,
                                                     @RequestParam String status) {
        if (!status.equalsIgnoreCase("ACCEPTED") && !status.equalsIgnoreCase("REJECTED")) {
            return ResponseEntity.badRequest().body("Status must be ACCEPTED or REJECTED");
        }

        JobApplication updated = applicationService.updateApplicationStatus(applicationId, status.toUpperCase());
        return ResponseEntity.ok(updated);
    }
}
