package com.jobportal.controller;

import com.jobportal.model.Job;
import com.jobportal.model.User;
import com.jobportal.service.JobService;
import com.jobportal.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
public class JobController {

    private final JobService jobService;
    private final UserService userService;

    public JobController(JobService jobService, UserService userService) {
        this.jobService = jobService;
        this.userService = userService;
    }

    @GetMapping("/jobs")
    public ResponseEntity<List<Job>> getAllJobs() {
        return ResponseEntity.ok(jobService.getAllJobs());
    }

    @PreAuthorize("hasRole('EMPLOYER')")
    @PostMapping("/employer/post-job")
    public ResponseEntity<?> postJob(@Valid @RequestBody Job job,
                                     @RequestHeader("Authorization") String token) {
        // Remove "Bearer " prefix if exists
        if (token.startsWith("Bearer ")) token = token.substring(7);

        String email = userService.getEmailFromToken(token);
        User employer = userService.getUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employer not found"));

        job.setEmployer(employer);
        job.setPostedDate(LocalDate.now());

        Job savedJob = jobService.postJob(job, employer);
        return ResponseEntity.ok(savedJob);
    }

    @PreAuthorize("hasRole('EMPLOYER')")
    @GetMapping("/employer/jobs")
    public ResponseEntity<List<Job>> getJobsByEmployer(@RequestHeader("Authorization") String token) {
        if (token.startsWith("Bearer ")) token = token.substring(7);

        String email = userService.getEmailFromToken(token);
        User employer = userService.getUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employer not found"));

        return ResponseEntity.ok(jobService.getJobsByEmployer(employer));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/jobs")
    public ResponseEntity<List<Job>> getAllJobsAdmin() {
        return ResponseEntity.ok(jobService.getAllJobs());
    }

    @PreAuthorize("hasRole('EMPLOYER') or hasRole('ADMIN')")
    @DeleteMapping("/jobs/{jobId}")
    public ResponseEntity<?> deleteJob(@PathVariable int jobId) {
        jobService.deleteJob(jobId);
        return ResponseEntity.ok("Job deleted successfully");
    }

    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<Job> getJobById(@PathVariable int jobId) {
        return jobService.getJobById(jobId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new RuntimeException("Job not found"));
    }
}
