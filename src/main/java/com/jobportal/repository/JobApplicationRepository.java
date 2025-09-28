package com.jobportal.repository;

import com.jobportal.model.Job;
import com.jobportal.model.JobApplication;
import com.jobportal.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Integer> {

    // Candidate-specific applications
    List<JobApplication> findByCandidate(User candidate);

    // Applications for a specific job
    List<JobApplication> findByJob(Job job);

    // Check if candidate already applied for a job
    Optional<JobApplication> findByCandidateAndJob(User candidate, Job job);

    // For employer: get all applications for a list of jobs
    List<JobApplication> findByJobIn(List<Job> jobs);
}
