package com.jobportal.repository;

import com.jobportal.model.Job;
import com.jobportal.model.JobApplication;
import com.jobportal.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Integer> {

    List<JobApplication> findByCandidate(User candidate);

    List<JobApplication> findByJob(Job job);

    Optional<JobApplication> findByCandidateAndJob(User candidate, Job job);

    List<JobApplication> findByJobIn(List<Job> jobs);
}
