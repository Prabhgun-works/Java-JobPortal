package com.jobportal.service;

import com.jobportal.exception.ResourceNotFound;
import com.jobportal.model.Job;
import com.jobportal.model.JobApplication;
import com.jobportal.model.User;
import com.jobportal.repository.JobApplicationRepository;
import com.jobportal.repository.JobRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
@Service
public class JobApplicationService {

    private final JobApplicationRepository applicationRepository;
    private final JobRepository jobRepository;

    // Better to use absolute path
    private final String uploadDir = System.getProperty("user.dir") + "/resumes/";

    public JobApplicationService(JobApplicationRepository applicationRepository,
                                 JobRepository jobRepository) {
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;

        // Create folder if not exists
        File folder = new File(uploadDir);
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    public JobApplication applyToJob(User candidate, int jobId, MultipartFile resumeFile) throws IOException {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFound("Job not found with id: " + jobId));

        String fileName = candidate.getId() + "_" + jobId + "_" + resumeFile.getOriginalFilename();
        File dest = new File(uploadDir + fileName);
        resumeFile.transferTo(dest);

        JobApplication application = new JobApplication();
        application.setCandidate(candidate);
        application.setJob(job);
        application.setResume(fileName);
        application.setStatus("PENDING");
        application.setAppliedDate(LocalDate.now());

        return applicationRepository.save(application);
    }

    public List<JobApplication> getApplicationsByCandidate(User candidate) {
        return applicationRepository.findByCandidate(candidate);
    }

    public List<JobApplication> getApplicationsForEmployer(User employer) {
        List<Job> employerJobs = jobRepository.findByEmployer(employer);
        if (employerJobs.isEmpty()) return List.of();
        return applicationRepository.findByJobIn(employerJobs);
    }

    public List<JobApplication> getAllApplications() {
        return applicationRepository.findAll();
    }

    public JobApplication updateApplicationStatus(int applicationId, String status) {
        JobApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFound("Application not found with id: " + applicationId));

        application.setStatus(status.toUpperCase());
        return applicationRepository.save(application);
    }
}
