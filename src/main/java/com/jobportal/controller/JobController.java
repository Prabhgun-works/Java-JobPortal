package com.jobportal.controller;

import com.jobportal.dto.JobDTO;
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
import java.util.stream.Collectors;

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
    public ResponseEntity<List<JobDTO>> getAllJobs() {
        List<JobDTO> dtos = jobService.getAllJobs().stream()
                .map(JobDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PreAuthorize("hasRole('EMPLOYER')")
    @PostMapping("/employer/post-job")
    public ResponseEntity<JobDTO> postJob(@Valid @RequestBody JobDTO jobDTO,
                                          @RequestHeader("Authorization") String token) {
        String email = extractEmailFromToken(token);
        User employer = userService.getUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employer not found"));

        Job job = jobDTO.toEntity();
        job.setEmployer(employer);
        job.setPostedDate(LocalDate.now());

        Job saved = jobService.postJob(job, employer);
        return ResponseEntity.ok(JobDTO.fromEntity(saved));
    }

    @PreAuthorize("hasRole('EMPLOYER')")
    @GetMapping("/employer/jobs")
    public ResponseEntity<List<JobDTO>> getJobsByEmployer(@RequestHeader("Authorization") String token) {
        String email = extractEmailFromToken(token);
        User employer = userService.getUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employer not found"));

        List<JobDTO> dtos = jobService.getJobsByEmployer(employer).stream()
                .map(JobDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/jobs")
    public ResponseEntity<List<JobDTO>> getAllJobsAdmin() {
        List<JobDTO> dtos = jobService.getAllJobs().stream()
                .map(JobDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PreAuthorize("hasRole('EMPLOYER') or hasRole('ADMIN')")
    @DeleteMapping("/jobs/{jobId}")
    public ResponseEntity<String> deleteJob(@PathVariable int jobId,
                                            @RequestHeader(value = "Authorization", required = false) String token) {
        jobService.deleteJob(jobId);
        return ResponseEntity.ok("Job deleted successfully");
    }

    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<JobDTO> getJobById(@PathVariable int jobId) {
        Job job = jobService.getJobById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        return ResponseEntity.ok(JobDTO.fromEntity(job));
    }

    private String extractEmailFromToken(String token) {
        if (token.startsWith("Bearer ")) token = token.substring(7);
        return userService.getEmailFromToken(token);
    }
}
