package com.jobportal.service;

import com.jobportal.exception.ResourceNotFound;
import com.jobportal.model.Job;
import com.jobportal.model.User;
import com.jobportal.repository.JobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class JobService {

    private final JobRepository jobRepository;

    public JobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @Transactional
    public Job postJob(Job job, User employer) {
        job.setEmployer(employer);
        return jobRepository.save(job);
    }


    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    public List<Job> getJobsByEmployer(User employer) {
        return jobRepository.findByEmployer(employer);
    }

    public Optional<Job> getJobById(int id) {
        return jobRepository.findById(id);
    }

    public void deleteJob(int id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFound("Job not found with id: " + id));
        jobRepository.delete(job);
    }
}
