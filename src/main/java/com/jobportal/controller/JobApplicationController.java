package com.jobportal.controller;

import com.jobportal.dto.JobApplicationDTO;
import com.jobportal.model.Job;
import com.jobportal.model.JobApplication;
import com.jobportal.model.User;
import com.jobportal.service.JobApplicationService;
import com.jobportal.service.JobService;
import com.jobportal.service.UserService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
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

    @PreAuthorize("hasRole('CANDIDATE')")
    @PostMapping("/candidate/apply/{jobId}")
    public ResponseEntity<JobApplicationDTO> applyForJob(@PathVariable int jobId,
                                                         @RequestHeader("Authorization") String token,
                                                         @RequestParam("resume") MultipartFile resume) throws IOException {

        if (token.startsWith("Bearer ")) token = token.substring(7);
        String email = userService.getEmailFromToken(token);
        User candidate = userService.getUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("Candidate not found"));

        JobApplication application = applicationService.applyToJob(candidate, jobId, resume);
        return ResponseEntity.ok(JobApplicationDTO.fromEntity(application));
    }
    @PreAuthorize("hasRole('CANDIDATE')")
    @GetMapping("/candidate/applications")
    public ResponseEntity<List<JobApplicationDTO>> getMyApplications(@RequestHeader("Authorization") String token) {
        if (token.startsWith("Bearer ")) token = token.substring(7);
        String email = userService.getEmailFromToken(token);
        User candidate = userService.getUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("Candidate not found"));

        List<JobApplicationDTO> dtos = applicationService.getApplicationsByCandidate(candidate)
                .stream().map(JobApplicationDTO::fromEntity).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PreAuthorize("hasRole('EMPLOYER')")
    @GetMapping("/employer/applications")
    public ResponseEntity<List<JobApplicationDTO>> getEmployerApplications(@RequestHeader("Authorization") String token) {
        if (token.startsWith("Bearer ")) token = token.substring(7);
        String email = userService.getEmailFromToken(token);
        User employer = userService.getUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employer not found"));

        List<JobApplicationDTO> dtos = applicationService.getApplicationsForEmployer(employer)
                .stream().map(JobApplicationDTO::fromEntity).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PreAuthorize("hasRole('EMPLOYER') or hasRole('ADMIN')")
    @GetMapping("/jobs/{jobId}/applications")
    public ResponseEntity<List<JobApplicationDTO>> getApplicationsForJob(@PathVariable int jobId) {
        Job job = jobService.getJobById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        List<JobApplicationDTO> dtos = applicationService.getApplicationsByJob(job)
                .stream().map(JobApplicationDTO::fromEntity).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PreAuthorize("hasRole('EMPLOYER') or hasRole('ADMIN')")
    @PutMapping("/applications/{applicationId}/status")
    public ResponseEntity<JobApplicationDTO> updateApplicationStatus(@PathVariable int applicationId,
                                                                     @RequestParam String status) {
        if (!status.equalsIgnoreCase("ACCEPTED") && !status.equalsIgnoreCase("REJECTED")) {
            throw new IllegalArgumentException("Status must be ACCEPTED or REJECTED");
        }

        JobApplication updated = applicationService.updateApplicationStatus(applicationId, status.toUpperCase());
        return ResponseEntity.ok(JobApplicationDTO.fromEntity(updated));
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/applications")
    public ResponseEntity<List<JobApplicationDTO>> getAllApplications() {
        List<JobApplicationDTO> dtos = applicationService.getAllApplications()
                .stream().map(JobApplicationDTO::fromEntity).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PreAuthorize("hasRole('CANDIDATE') or hasRole('EMPLOYER') or hasRole('ADMIN')")
    @GetMapping("/applications/{applicationId}/resume")
    public ResponseEntity<ByteArrayResource> downloadResume(@PathVariable int applicationId) {
        JobApplication application = applicationService.getApplicationById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (application.getResume() == null || application.getResume().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        byte[] resumeBytes = applicationService.getResumeBytes(application.getResume());
        ByteArrayResource resource = new ByteArrayResource(resumeBytes);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + application.getResume() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
